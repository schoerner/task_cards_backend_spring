package de.acosci.tasks.service.impl;

import de.acosci.tasks.model.dto.TaskCreateDTO;
import de.acosci.tasks.model.dto.TaskResponseDTO;
import de.acosci.tasks.model.dto.TaskUpdateDTO;
import de.acosci.tasks.model.dto.TimeRecordResponseDTO;
import de.acosci.tasks.model.entity.BoardColumn;
import de.acosci.tasks.model.entity.Project;
import de.acosci.tasks.model.entity.Task;
import de.acosci.tasks.model.entity.TaskLabel;
import de.acosci.tasks.model.entity.TimeRecord;
import de.acosci.tasks.model.entity.User;
import de.acosci.tasks.model.mapper.TaskMapper;
import de.acosci.tasks.repository.BoardColumnRepository;
import de.acosci.tasks.repository.ProjectRepository;
import de.acosci.tasks.repository.TaskLabelRepository;
import de.acosci.tasks.repository.TaskRepository;
import de.acosci.tasks.repository.TimeRecordRepository;
import de.acosci.tasks.repository.UserRepository;
import de.acosci.tasks.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final BoardColumnRepository boardColumnRepository;
    private final UserRepository userRepository;
    private final TaskLabelRepository taskLabelRepository;
    private final TimeRecordRepository timeRecordRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getTasksByProject(Long projectId) {
        List<Task> tasks = taskRepository.findAllByProject_IdAndArchivedFalse(projectId);
        tasks.forEach(this::synchronizeTrackedMinutesFromRecords);
        return tasks.stream()
                .map(TaskMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponseDTO getTaskById(Long taskId) {
        Task task = requireTask(taskId);
        synchronizeTrackedMinutesFromRecords(task);
        return TaskMapper.toResponseDTO(task);
    }

    @Override
    public TaskResponseDTO createTask(TaskCreateDTO dto) {
        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + dto.getProjectId()));

        BoardColumn boardColumn = resolveBoardColumn(dto.getProjectId(), dto.getBoardColumnId());
        User currentUser = getCurrentUser();

        Task task = new Task();
        task.setProject(project);
        task.setBoardColumn(boardColumn);
        task.setCreator(currentUser);
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setPriority(dto.getPriority());
        task.setDueDate(dto.getDueDate());
        task.setEstimatedMinutes(dto.getEstimatedMinutes() != null ? dto.getEstimatedMinutes() : 0);
        task.setTrackedMinutes(0);
        task.setAssignees(resolveUsers(dto.getAssigneeIds()));
        task.setLabels(resolveLabels(dto.getLabelIds()));
        return TaskMapper.toResponseDTO(taskRepository.save(task));
    }

    @Override
    public TaskResponseDTO updateTask(Long taskId, TaskUpdateDTO dto) {
        Task task = requireTask(taskId);
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());

        if (dto.getBoardColumnId() != null) {
            task.setBoardColumn(resolveBoardColumn(task.getProject().getId(), dto.getBoardColumnId()));
        }
        if (dto.getPriority() != null) {
            task.setPriority(dto.getPriority());
        }

        task.setDueDate(dto.getDueDate());

        if (dto.getEstimatedMinutes() != null) {
            task.setEstimatedMinutes(dto.getEstimatedMinutes());
        }

        /*
         * trackedMinutes remains a derived/cache field from time_records.
         * Do not overwrite it blindly through the generic update endpoint.
         */
        synchronizeTrackedMinutesFromRecords(task);

        task.setArchived(dto.isArchived());
        task.setAssignees(resolveUsers(dto.getAssigneeIds()));
        task.setLabels(resolveLabels(dto.getLabelIds()));
        return TaskMapper.toResponseDTO(taskRepository.save(task));
    }

    @Override
    public TaskResponseDTO moveTask(Long taskId, Long boardColumnId) {
        Task task = requireTask(taskId);
        task.setBoardColumn(resolveBoardColumn(task.getProject().getId(), boardColumnId));
        return TaskMapper.toResponseDTO(taskRepository.save(task));
    }

    @Override
    public TaskResponseDTO archiveTask(Long taskId) {
        Task task = requireTask(taskId);
        task.setArchived(true);
        return TaskMapper.toResponseDTO(taskRepository.save(task));
    }

    @Override
    public TaskResponseDTO restoreTask(Long taskId) {
        Task task = requireTask(taskId);
        task.setArchived(false);
        return TaskMapper.toResponseDTO(taskRepository.save(task));
    }

    @Override
    public void deleteTask(Long taskId) {
        taskRepository.deleteById(taskId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeRecordResponseDTO> getTimeRecords(Long taskId) {
        requireTask(taskId);
        return timeRecordRepository.findAllByTask_IdOrderByTimeStartDesc(taskId).stream()
                .map(TaskMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TimeRecordResponseDTO getActiveTimeRecord(Long taskId) {
        requireTask(taskId);
        return timeRecordRepository.findFirstByTask_IdAndTimeEndIsNullOrderByTimeStartDesc(taskId)
                .map(TaskMapper::toResponseDTO)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isActive(Long taskId) {
        return getActiveTimeRecord(taskId) != null;
    }

    @Override
    public TaskResponseDTO startTimeTracking(Long taskId) {
        Task task = requireTask(taskId);

        if (isActive(taskId)) {
            throw new IllegalStateException("Task already has an active time record: " + taskId);
        }

        TimeRecord newTimeRecord = new TimeRecord(task);
        timeRecordRepository.save(newTimeRecord);
        task.getTimeRecords().add(newTimeRecord);

        synchronizeTrackedMinutesFromRecords(task);
        return TaskMapper.toResponseDTO(taskRepository.save(task));
    }

    @Override
    public TaskResponseDTO stopTimeTracking(Long taskId) {
        Task task = requireTask(taskId);

        TimeRecord activeTimeRecord = timeRecordRepository
                .findFirstByTask_IdAndTimeEndIsNullOrderByTimeStartDesc(taskId)
                .orElseThrow(() -> new IllegalStateException("No active time record found for task " + taskId));

        activeTimeRecord.setTimeEnd(new Date());
        timeRecordRepository.save(activeTimeRecord);

        synchronizeTrackedMinutesFromRecords(task);
        return TaskMapper.toResponseDTO(taskRepository.save(task));
    }

    private Task requireTask(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));
    }

    private BoardColumn resolveBoardColumn(Long projectId, Long boardColumnId) {
        if (boardColumnId != null) {
            return boardColumnRepository.findById(boardColumnId)
                    .filter(column -> column.getProject().getId().equals(projectId))
                    .orElseThrow(() -> new IllegalArgumentException("Board column not found: " + boardColumnId));
        }
        return boardColumnRepository.findByProject_IdAndName(projectId, "Not assigned")
                .orElseThrow(() -> new IllegalStateException("Mandatory default column 'Not assigned' is missing."));
    }

    private Set<User> resolveUsers(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new LinkedHashSet<>();
        }
        return new LinkedHashSet<>(userRepository.findAllById(userIds));
    }

    private Set<TaskLabel> resolveLabels(Set<Long> labelIds) {
        if (labelIds == null || labelIds.isEmpty()) {
            return new LinkedHashSet<>();
        }
        return new LinkedHashSet<>(taskLabelRepository.findAllById(labelIds));
    }

    private void synchronizeTrackedMinutesFromRecords(Task task) {
        int trackedMinutes = task.getTimeRecords().stream()
                .filter(timeRecord -> timeRecord.getTimeStart() != null && timeRecord.getTimeEnd() != null)
                .mapToInt(timeRecord -> {
                    long diffInMillis = timeRecord.getTimeEnd().getTime() - timeRecord.getTimeStart().getTime();
                    return (int) TimeUnit.MILLISECONDS.toMinutes(Math.max(diffInMillis, 0L));
                })
                .sum();
        task.setTrackedMinutes(trackedMinutes);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + email));
    }
}