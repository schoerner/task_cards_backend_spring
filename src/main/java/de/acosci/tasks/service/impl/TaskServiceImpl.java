package de.acosci.tasks.service.impl;

import de.acosci.tasks.model.dto.*;
import de.acosci.tasks.model.entity.*;
import de.acosci.tasks.model.mapper.TaskMapper;
import de.acosci.tasks.repository.*;
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
    private final ProjectMemberRepository projectMemberRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getTasksByProject(Long projectId) {
        List<Task> tasks = taskRepository.findAllByProject_IdAndArchivedFalse(projectId);
        tasks.forEach(this::synchronizeTrackedMinutesFromRecords);
        return tasks.stream().map(TaskMapper::toResponseDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponseDTO getTaskById(Long taskId) {
        Task task = getTaskEntityById(taskId);
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
        task.setStartAt(dto.getStartAt());
        task.setLocation(dto.getLocation());
        applyCalendarReminders(task, dto.getCalendarReminders());
        task.setEstimatedMinutes(dto.getEstimatedMinutes() != null ? dto.getEstimatedMinutes() : 0);
        task.setTrackedMinutes(0);
        task.setAssignees(resolveProjectMembers(project.getId(), dto.getAssigneeIds()));
        task.setLabels(resolveLabels(dto.getLabelIds()));
        return TaskMapper.toResponseDTO(taskRepository.save(task));
    }

    private Set<User> resolveProjectMembers(Long projectId, Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new LinkedHashSet<>();
        }

        Set<User> users = new LinkedHashSet<>();

        for (Long userId : userIds) {
            boolean isProjectMember = projectMemberRepository.existsByProject_IdAndUser_Id(projectId, userId);
            if (!isProjectMember) {
                throw new IllegalArgumentException("User is not a member of project: " + userId);
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
            users.add(user);
        }

        return users;
    }

    @Override
    public TaskResponseDTO updateTask(Long taskId, TaskUpdateDTO dto) {
        Task task = getTaskEntityById(taskId);
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());

        if (dto.getBoardColumnId() != null) {
            task.setBoardColumn(resolveBoardColumn(task.getProject().getId(), dto.getBoardColumnId()));
        }
        if (dto.getPriority() != null) {
            task.setPriority(dto.getPriority());
        }

        task.setDueDate(dto.getDueDate());
        task.setStartAt(dto.getStartAt());
        task.setLocation(dto.getLocation());
        applyCalendarReminders(task, dto.getCalendarReminders());

        if (dto.getEstimatedMinutes() != null) {
            task.setEstimatedMinutes(dto.getEstimatedMinutes());
        }

        synchronizeTrackedMinutesFromRecords(task);
        task.setArchived(dto.isArchived());
        task.setAssignees(resolveProjectMembers(task.getProject().getId(), dto.getAssigneeIds()));
        task.setLabels(resolveLabels(dto.getLabelIds()));
        return TaskMapper.toResponseDTO(taskRepository.save(task));
    }

    @Override
    public TaskResponseDTO moveTask(Long taskId, Long boardColumnId) {
        Task task = getTaskEntityById(taskId);
        task.setBoardColumn(resolveBoardColumn(task.getProject().getId(), boardColumnId));
        return TaskMapper.toResponseDTO(taskRepository.save(task));
    }

    @Override
    public TaskResponseDTO archiveTask(Long taskId) {
        Task task = getTaskEntityById(taskId);
        task.setArchived(true);
        return TaskMapper.toResponseDTO(taskRepository.save(task));
    }

    @Override
    public TaskResponseDTO restoreTask(Long taskId) {
        Task task = getTaskEntityById(taskId);
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
        getTaskEntityById(taskId);
        return timeRecordRepository.findAllByTask_IdOrderByTimeStartDesc(taskId)
                .stream()
                .map(TaskMapper::toTimeRecordResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TimeRecordResponseDTO getActiveTimeRecord(Long taskId) {
        getTaskEntityById(taskId);
        return timeRecordRepository.findFirstByTask_IdAndTimeEndIsNullOrderByTimeStartDesc(taskId)
                .map(TaskMapper::toTimeRecordResponseDTO)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isActive(Long taskId) {
        return getActiveTimeRecord(taskId) != null;
    }

    @Override
    public TaskResponseDTO startTimeTracking(Long taskId) {
        Task task = getTaskEntityById(taskId);

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
        Task task = getTaskEntityById(taskId);

        TimeRecord activeTimeRecord = timeRecordRepository
                .findFirstByTask_IdAndTimeEndIsNullOrderByTimeStartDesc(taskId)
                .orElseThrow(() -> new IllegalStateException("No active time record found for task " + taskId));

        activeTimeRecord.setTimeEnd(new Date());
        timeRecordRepository.save(activeTimeRecord);

        synchronizeTrackedMinutesFromRecords(task);
        return TaskMapper.toResponseDTO(taskRepository.save(task));
    }

    private Task getTaskEntityById(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));
    }

    private BoardColumn resolveBoardColumn(Long projectId, Long boardColumnId) {
        if (boardColumnId != null) {
            return boardColumnRepository.findById(boardColumnId)
                    .filter(column -> column.getProject().getId().equals(projectId))
                    .orElseThrow(() -> new IllegalArgumentException("Board column not found: " + boardColumnId));
        }
        return boardColumnRepository
                .findFirstByProjectIdAndDeletableFalse(projectId)
                .orElseGet(() -> boardColumnRepository
                        .findFirstByProjectIdOrderByPositionAsc(projectId)
                        .orElseThrow(() -> new IllegalStateException("No board column available for project " + projectId)));
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

    private void applyCalendarReminders(Task task, Set<TaskCalendarReminderDTO> reminderDTOs) {
        task.getCalendarReminders().clear();

        if (reminderDTOs == null || reminderDTOs.isEmpty()) {
            return;
        }

        for (TaskCalendarReminderDTO dto : reminderDTOs) {
            if (dto.getMinutesBefore() == null) {
                continue;
            }

            TaskCalendarReminder reminder = new TaskCalendarReminder();
            reminder.setTask(task);
            reminder.setMinutesBefore(dto.getMinutesBefore());
            reminder.setActionType(dto.getActionType() != null ? dto.getActionType() : "DISPLAY");
            reminder.setMessage(dto.getMessage());
            task.getCalendarReminders().add(reminder);
        }
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + email));
    }
}
