package de.acosci.tasks.model.mapper;

import de.acosci.tasks.model.dto.*;
import de.acosci.tasks.model.entity.Task;
import de.acosci.tasks.model.entity.TaskLabel;
import de.acosci.tasks.model.entity.TimeRecord;
import de.acosci.tasks.model.entity.User;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public final class TaskMapper {

    private TaskMapper() {
    }

    public static TaskResponseDTO toResponseDTO(Task task) {
        if (task == null) {
            return null;
        }

        TaskResponseDTO dto = new TaskResponseDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setProject(ProjectMapper.toProjectResponseDTO(task.getProject()));
        dto.setBoardColumn(BoardColumnMapper.toResponseDTO(task.getBoardColumn()));
        dto.setCreator(toUserSummaryDTO(task.getCreator()));
        dto.setPriority(task.getPriority());
        dto.setDueDate(task.getDueDate());
        dto.setArchived(task.isArchived());
        dto.setEstimatedMinutes(task.getEstimatedMinutes());
        dto.setTrackedMinutes(task.getTrackedMinutes());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());
        dto.setAssignees(toUserSummaryDTOSet(task.getAssignees()));
        dto.setLabels(toTaskLabelDTOSet(task.getLabels()));
        dto.setActive(task.isActive());
        return dto;
    }

    public static TimeRecordResponseDTO toResponseDTO(TimeRecord timeRecord) {
        if (timeRecord == null) {
            return null;
        }

        TimeRecordResponseDTO dto = new TimeRecordResponseDTO();
        dto.setId(timeRecord.getId());
        dto.setTimeStart(timeRecord.getTimeStart());
        dto.setTimeEnd(timeRecord.getTimeEnd());
        dto.setTaskId(timeRecord.getTask() != null ? timeRecord.getTask().getId() : null);
        return dto;
    }

    private static UserSummaryDTO toUserSummaryDTO(User user) {
        if (user == null) {
            return null;
        }

        UserSummaryDTO dto = new UserSummaryDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        return dto;
    }

    private static Set<UserSummaryDTO> toUserSummaryDTOSet(Set<User> users) {
        if (users == null || users.isEmpty()) {
            return new LinkedHashSet<>();
        }

        return users.stream()
                .map(TaskMapper::toUserSummaryDTO)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static Set<TaskLabelResponseDTO> toTaskLabelDTOSet(Set<TaskLabel> labels) {
        if (labels == null || labels.isEmpty()) {
            return new LinkedHashSet<>();
        }

        return labels.stream()
                .map(TaskMapper::toTaskLabelDTO)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static TaskLabelResponseDTO toTaskLabelDTO(TaskLabel label) {
        if (label == null) {
            return null;
        }

        TaskLabelResponseDTO dto = new TaskLabelResponseDTO();
        dto.setId(label.getId());
        dto.setName(label.getName());
        dto.setColor(label.getColor());
        return dto;
    }
}
