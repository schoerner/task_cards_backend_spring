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
    private final UserFavoriteTaskRepository userFavoriteTaskRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getTasksByProject(Long projectId) {
        return taskRepository.findAllByProject_IdAndArchivedFalse(projectId).stream()
                .peek(this::synchronizeTrackedMinutesFromRecords)
                .map(this::toResponseDtoForCurrentUser)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponseDTO getTaskById(Long taskId) {
        Task task = getTaskEntityById(taskId);
        synchronizeTrackedMinutesFromRecords(task);
        return toResponseDtoForCurrentUser(task);
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
        task.setPosition(nextTaskPosition(project.getId(), boardColumn.getId()));
        task.setAssignees(resolveProjectMembers(project.getId(), dto.getAssigneeIds()));
        task.setLabels(resolveLabels(dto.getLabelIds()));

        return toResponseDtoForCurrentUser(taskRepository.save(task));
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

        if (dto.getBoardColumnId() != null && !dto.getBoardColumnId().equals(task.getBoardColumn().getId())) {
            BoardColumn targetColumn = resolveBoardColumn(task.getProject().getId(), dto.getBoardColumnId());
            task.setBoardColumn(targetColumn);
            insertTaskIntoColumn(task, targetColumn.getId(), null);
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

        return toResponseDtoForCurrentUser(taskRepository.save(task));
    }

    private void applyTaskOrderToColumn(Long projectId, Long boardColumnId, List<Long> orderedTaskIds) {
        BoardColumn column = resolveBoardColumn(projectId, boardColumnId);

        List<Task> existingTasks = taskRepository
                .findAllByProject_IdAndBoardColumn_IdAndArchivedFalseOrderByPositionAscIdAsc(projectId, boardColumnId);

        if (existingTasks.size() != orderedTaskIds.size()) {
            throw new IllegalArgumentException("Task order size does not match tasks in target column.");
        }

        List<Long> existingIds = existingTasks.stream().map(Task::getId).sorted().toList();
        List<Long> incomingIds = orderedTaskIds.stream().sorted().toList();

        if (!existingIds.equals(incomingIds)) {
            throw new IllegalArgumentException("Task order does not match tasks in target column.");
        }

        java.util.Map<Long, Task> byId = existingTasks.stream()
                .collect(java.util.stream.Collectors.toMap(Task::getId, task -> task));

        for (int i = 0; i < orderedTaskIds.size(); i++) {
            Long taskId = orderedTaskIds.get(i);
            Task task = byId.get(taskId);
            task.setBoardColumn(column);
            task.setPosition(i);
        }

        taskRepository.saveAll(existingTasks);
    }

    @Override
    public void reorderTasksInColumn(Long projectId, Long boardColumnId, List<Long> orderedTaskIds) {
        applyTaskOrderToColumn(projectId, boardColumnId, orderedTaskIds);
    }

    @Override
    public void moveTaskBetweenColumns(Long projectId,
                                       Long sourceColumnId,
                                       Long targetColumnId,
                                       List<Long> sourceTaskIds,
                                       List<Long> targetTaskIds) {

        BoardColumn sourceColumn = resolveBoardColumn(projectId, sourceColumnId);
        BoardColumn targetColumn = resolveBoardColumn(projectId, targetColumnId);

        List<Task> sourceTasks = taskRepository
                .findAllByProject_IdAndBoardColumn_IdAndArchivedFalseOrderByPositionAscIdAsc(projectId, sourceColumnId);

        List<Task> targetTasks = taskRepository
                .findAllByProject_IdAndBoardColumn_IdAndArchivedFalseOrderByPositionAscIdAsc(projectId, targetColumnId);

        java.util.Map<Long, Task> sourceById = sourceTasks.stream()
                .collect(java.util.stream.Collectors.toMap(Task::getId, task -> task));
        java.util.Map<Long, Task> targetById = targetTasks.stream()
                .collect(java.util.stream.Collectors.toMap(Task::getId, task -> task));

        List<Long> existingSourceIds = sourceTasks.stream().map(Task::getId).sorted().toList();
        List<Long> existingTargetIds = targetTasks.stream().map(Task::getId).sorted().toList();

        List<Long> incomingSourceIds = sourceTaskIds.stream().sorted().toList();
        List<Long> incomingTargetIds = targetTaskIds.stream().sorted().toList();

        if (sourceColumnId.equals(targetColumnId)) {
            if (!existingSourceIds.equals(incomingTargetIds)) {
                throw new IllegalArgumentException("Task order does not match tasks in column.");
            }

            for (int i = 0; i < targetTaskIds.size(); i++) {
                Task task = sourceById.get(targetTaskIds.get(i));
                task.setBoardColumn(targetColumn);
                task.setPosition(i);
            }

            taskRepository.saveAll(sourceTasks);
            return;
        }

        java.util.Set<Long> removedFromSource = new java.util.HashSet<>(existingSourceIds);
        removedFromSource.removeAll(incomingSourceIds);

        if (removedFromSource.size() != 1) {
            throw new IllegalArgumentException("Exactly one task must be moved out of source column.");
        }

        Long movedTaskId = removedFromSource.iterator().next();

        java.util.List<Long> expectedSourceIds = existingSourceIds.stream()
                .filter(id -> !id.equals(movedTaskId))
                .sorted()
                .toList();

        if (!expectedSourceIds.equals(incomingSourceIds)) {
            throw new IllegalArgumentException("Source task order does not match expected remaining tasks in source column.");
        }

        java.util.List<Long> expectedTargetIds = new java.util.ArrayList<>(existingTargetIds);
        expectedTargetIds.add(movedTaskId);
        expectedTargetIds = expectedTargetIds.stream().sorted().toList();

        if (!expectedTargetIds.equals(incomingTargetIds)) {
            throw new IllegalArgumentException("Target task order does not match expected tasks in target column.");
        }

        Task movedTask = sourceById.get(movedTaskId);
        if (movedTask == null) {
            throw new IllegalArgumentException("Moved task not found in source column.");
        }

        for (int i = 0; i < sourceTaskIds.size(); i++) {
            Task task = sourceById.get(sourceTaskIds.get(i));
            task.setBoardColumn(sourceColumn);
            task.setPosition(i);
        }

        for (int i = 0; i < targetTaskIds.size(); i++) {
            Long taskId = targetTaskIds.get(i);
            Task task = taskId.equals(movedTaskId)
                    ? movedTask
                    : targetById.get(taskId);

            if (task == null) {
                throw new IllegalArgumentException("Task " + taskId + " not found for target column.");
            }

            task.setBoardColumn(targetColumn);
            task.setPosition(i);
        }

        taskRepository.saveAll(sourceTasks);
        taskRepository.save(movedTask);
        taskRepository.saveAll(targetTasks);
    }

    @Override
    public TaskResponseDTO archiveTask(Long taskId) {
        Task task = getTaskEntityById(taskId);
        task.setArchived(true);
        normalizeTaskPositions(task.getProject().getId(), task.getBoardColumn().getId(), task.getId());
        return toResponseDtoForCurrentUser(taskRepository.save(task));
    }

    @Override
    public TaskResponseDTO restoreTask(Long taskId) {
        Task task = getTaskEntityById(taskId);
        task.setArchived(false);
        task.setPosition(nextTaskPosition(task.getProject().getId(), task.getBoardColumn().getId()));
        return toResponseDtoForCurrentUser(taskRepository.save(task));
    }

    @Override
    public void deleteTask(Long taskId) {
        Task task = getTaskEntityById(taskId);
        Long projectId = task.getProject().getId();
        Long boardColumnId = task.getBoardColumn().getId();
        taskRepository.delete(task);
        normalizeTaskPositions(projectId, boardColumnId, null);
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
        User currentUser = getCurrentUser();
        getTaskEntityById(taskId);

        return timeRecordRepository
                .findFirstByTask_IdAndUser_IdAndTimeEndIsNullOrderByTimeStartDesc(taskId, currentUser.getId())
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
        User currentUser = getCurrentUser();

        TimeRecord activeRecordOfUser = timeRecordRepository
                .findFirstByUser_IdAndTimeEndIsNullOrderByTimeStartDesc(currentUser.getId())
                .orElse(null);

        if (activeRecordOfUser != null) {
            Long activeTaskId = activeRecordOfUser.getTask().getId();

            if (activeTaskId.equals(taskId)) {
                synchronizeTrackedMinutesFromRecords(task);
                return toResponseDtoForCurrentUser(task);
            }

            activeRecordOfUser.setTimeEnd(new Date());
            timeRecordRepository.save(activeRecordOfUser);

            Task previousTask = activeRecordOfUser.getTask();
            synchronizeTrackedMinutesFromRecords(previousTask);
            taskRepository.save(previousTask);
        }

        TimeRecord sameTaskActiveRecord = timeRecordRepository
                .findFirstByTask_IdAndUser_IdAndTimeEndIsNullOrderByTimeStartDesc(taskId, currentUser.getId())
                .orElse(null);

        if (sameTaskActiveRecord == null) {
            TimeRecord newTimeRecord = new TimeRecord(task, currentUser);
            timeRecordRepository.save(newTimeRecord);
            task.getTimeRecords().add(newTimeRecord);
        }

        synchronizeTrackedMinutesFromRecords(task);
        return toResponseDtoForCurrentUser(taskRepository.save(task));
    }

    @Override
    public TaskResponseDTO stopTimeTracking(Long taskId) {
        User currentUser = getCurrentUser();
        Task task = getTaskEntityById(taskId);

        TimeRecord activeTimeRecord = timeRecordRepository
                .findFirstByTask_IdAndUser_IdAndTimeEndIsNullOrderByTimeStartDesc(taskId, currentUser.getId())
                .orElseThrow(() -> new IllegalStateException("No active time record found for task " + taskId));

        activeTimeRecord.setTimeEnd(new Date());
        timeRecordRepository.save(activeTimeRecord);

        synchronizeTrackedMinutesFromRecords(task);
        return toResponseDtoForCurrentUser(taskRepository.save(task));
    }

    @Override
    public TaskResponseDTO setFavorite(Long taskId, boolean favorite) {
        Task task = getTaskEntityById(taskId);
        User currentUser = getCurrentUser();

        userFavoriteTaskRepository.findByUser_IdAndTask_Id(currentUser.getId(), taskId)
                .ifPresent(existing -> {
                    if (!favorite) {
                        userFavoriteTaskRepository.delete(existing);
                    }
                });

        if (favorite && !userFavoriteTaskRepository.existsByUser_IdAndTask_Id(currentUser.getId(), taskId)) {
            userFavoriteTaskRepository.save(new UserFavoriteTask(currentUser, task));
        }

        return toResponseDtoForCurrentUser(task);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getFocusTasks(Integer limit) {
        User currentUser = getCurrentUser();
        int effectiveLimit = Math.max(1, Math.min(limit != null ? limit : 10, 50));

        List<Long> favoriteTaskIds = userFavoriteTaskRepository.findAllByUser_Id(currentUser.getId())
                .stream()
                .map(userFavoriteTask -> userFavoriteTask.getTask().getId())
                .toList();

        return taskRepository.findAllByAssignees_IdAndArchivedFalse(currentUser.getId()).stream()
                .peek(this::synchronizeTrackedMinutesFromRecords)
                .sorted((a, b) -> compareForFocus(a, b, favoriteTaskIds))
                .limit(effectiveLimit)
                .map(this::toResponseDtoForCurrentUser)
                .toList();
    }

    private int compareForFocus(Task a, Task b, List<Long> favoriteTaskIds) {
        boolean aFavorite = favoriteTaskIds.contains(a.getId());
        boolean bFavorite = favoriteTaskIds.contains(b.getId());

        if (aFavorite != bFavorite) {
            return aFavorite ? -1 : 1;
        }

        int priorityCompare = Integer.compare(priorityRank(a), priorityRank(b));
        if (priorityCompare != 0) {
            return priorityCompare;
        }

        DateScore aScore = dateScore(a);
        DateScore bScore = dateScore(b);

        int dueDateCompare = aScore.compareTo(bScore);
        if (dueDateCompare != 0) {
            return dueDateCompare;
        }

        return Long.compare(a.getId(), b.getId());
    }

    private int priorityRank(Task task) {
        return switch (task.getPriority()) {
            case URGENT -> 0;
            case HIGH -> 1;
            case MEDIUM -> 2;
            case LOW -> 3;
        };
    }

    private DateScore dateScore(Task task) {
        Date due = task.getDueDate() != null ? Date.from(task.getDueDate().toInstant()) : null;
        Date start = task.getStartAt() != null ? Date.from(task.getStartAt().toInstant()) : null;
        Date relevant = due != null ? due : start;

        if (relevant == null) {
            return new DateScore(1, Long.MAX_VALUE);
        }

        return new DateScore(0, relevant.getTime());
    }

    private record DateScore(int nullRank, long timestamp) implements Comparable<DateScore> {
        @Override
        public int compareTo(DateScore other) {
            int nullRankCompare = Integer.compare(this.nullRank, other.nullRank);
            if (nullRankCompare != 0) {
                return nullRankCompare;
            }
            return Long.compare(this.timestamp, other.timestamp);
        }
    }

    private TaskResponseDTO toResponseDtoForCurrentUser(Task task) {
        synchronizeTrackedMinutesFromRecords(task);

        User currentUser = getCurrentUser();
        TaskResponseDTO dto = TaskMapper.toResponseDTO(task);

        dto.setPosition(task.getPosition());
        dto.setFavorite(userFavoriteTaskRepository.existsByUser_IdAndTask_Id(currentUser.getId(), task.getId()));
        dto.setActive(
                timeRecordRepository.findFirstByTask_IdAndUser_IdAndTimeEndIsNullOrderByTimeStartDesc(
                        task.getId(), currentUser.getId()
                ).isPresent()
        );

        return dto;
    }

    private int nextTaskPosition(Long projectId, Long boardColumnId) {
        return taskRepository.findAllByProject_IdAndBoardColumn_IdAndArchivedFalseOrderByPositionAscIdAsc(projectId, boardColumnId)
                .stream()
                .map(Task::getPosition)
                .filter(position -> position != null)
                .max(Integer::compareTo)
                .orElse(-1) + 1;
    }

    private void insertTaskIntoColumn(Task movedTask, Long targetColumnId, Long beforeTaskId) {
        List<Task> tasksInTarget = taskRepository
                .findAllByProject_IdAndBoardColumn_IdAndArchivedFalseOrderByPositionAscIdAsc(
                        movedTask.getProject().getId(),
                        targetColumnId
                )
                .stream()
                .filter(task -> !task.getId().equals(movedTask.getId()))
                .toList();

        List<Task> reordered = new java.util.ArrayList<>(tasksInTarget);

        int targetIndex = reordered.size();
        if (beforeTaskId != null) {
            for (int i = 0; i < reordered.size(); i++) {
                if (reordered.get(i).getId().equals(beforeTaskId)) {
                    targetIndex = i;
                    break;
                }
            }
        }

        reordered.add(targetIndex, movedTask);

        for (int i = 0; i < reordered.size(); i++) {
            Task task = reordered.get(i);
            task.setBoardColumn(resolveBoardColumn(movedTask.getProject().getId(), targetColumnId));
            task.setPosition(i);
        }

        taskRepository.saveAll(reordered);
    }

    private void normalizeTaskPositions(Long projectId, Long boardColumnId, Long excludedTaskId) {
        List<Task> tasks = taskRepository
                .findAllByProject_IdAndBoardColumn_IdAndArchivedFalseOrderByPositionAscIdAsc(projectId, boardColumnId)
                .stream()
                .filter(task -> excludedTaskId == null || !task.getId().equals(excludedTaskId))
                .toList();

        for (int i = 0; i < tasks.size(); i++) {
            tasks.get(i).setPosition(i);
        }

        taskRepository.saveAll(tasks);
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