package de.acosci.tasks.model.mapper;

import de.acosci.tasks.model.dto.*;
import de.acosci.tasks.model.entity.*;

import java.util.LinkedHashSet;
import java.util.Set;

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
        dto.setProjectId(task.getProject() != null ? task.getProject().getId() : null);
        dto.setBoardColumnId(task.getBoardColumn() != null ? task.getBoardColumn().getId() : null);
        dto.setCreatorId(task.getCreator() != null ? task.getCreator().getId() : null);
        dto.setProject(toProjectSummary(task));
        dto.setBoardColumn(toBoardColumnSummary(task));
        dto.setPriority(task.getPriority());
        dto.setDueDate(task.getDueDate());
        dto.setArchived(task.isArchived());
        dto.setEstimatedMinutes(task.getEstimatedMinutes());
        dto.setTrackedMinutes(task.getTrackedMinutes());
        dto.setActive(task.isActive());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());
        dto.setStartAt(task.getStartAt());
        dto.setLocation(task.getLocation());

        Set<TaskCalendarReminderDTO> reminders = new LinkedHashSet<>();
        if (task.getCalendarReminders() != null) {
            for (TaskCalendarReminder reminder : task.getCalendarReminders()) {
                TaskCalendarReminderDTO reminderDTO = new TaskCalendarReminderDTO();
                reminderDTO.setId(reminder.getId());
                reminderDTO.setMinutesBefore(reminder.getMinutesBefore());
                reminderDTO.setActionType(reminder.getActionType());
                reminderDTO.setMessage(reminder.getMessage());
                reminders.add(reminderDTO);
            }
        }
        dto.setCalendarReminders(reminders);

        Set<UserSummaryDTO> assignees = new LinkedHashSet<>();
        if (task.getAssignees() != null) {
            for (User assignee : task.getAssignees()) {
                assignees.add(toUserSummary(assignee));
            }
        }
        dto.setAssignees(assignees);

        Set<TaskLabelResponseDTO> labels = new LinkedHashSet<>();
        if (task.getLabels() != null) {
            for (TaskLabel label : task.getLabels()) {
                labels.add(toTaskLabelResponseDTO(label));
            }
        }
        dto.setLabels(labels);
        return dto;
    }

    private static ProjectSummaryDTO toProjectSummary(Task task) {
        if (task == null || task.getProject() == null) {
            return null;
        }

        ProjectSummaryDTO dto = new ProjectSummaryDTO();
        dto.setId(task.getProject().getId());
        dto.setName(task.getProject().getName());
        return dto;
    }

    private static BoardColumnSummaryDTO toBoardColumnSummary(Task task) {
        if (task == null || task.getBoardColumn() == null) {
            return null;
        }

        BoardColumnSummaryDTO dto = new BoardColumnSummaryDTO();
        dto.setId(task.getBoardColumn().getId());
        dto.setName(task.getBoardColumn().getName());
        return dto;
    }

    public static TimeRecordResponseDTO toTimeRecordResponseDTO(TimeRecord timeRecord) {
        if (timeRecord == null) {
            return null;
        }

        TimeRecordResponseDTO dto = new TimeRecordResponseDTO();
        dto.setId(timeRecord.getId());
        dto.setTaskId(timeRecord.getTask() != null ? timeRecord.getTask().getId() : null);
        dto.setTimeStart(timeRecord.getTimeStart());
        dto.setTimeEnd(timeRecord.getTimeEnd());
        dto.setActive(timeRecord.getTimeEnd() == null);
        return dto;
    }

    private static UserSummaryDTO toUserSummary(User user) {
        UserSummaryDTO dto = new UserSummaryDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        return dto;
    }

    private static TaskLabelResponseDTO toTaskLabelResponseDTO(TaskLabel label) {
        TaskLabelResponseDTO dto = new TaskLabelResponseDTO();
        dto.setId(label.getId());
        dto.setName(label.getName());
        dto.setColor(label.getColor());
        return dto;
    }

}
