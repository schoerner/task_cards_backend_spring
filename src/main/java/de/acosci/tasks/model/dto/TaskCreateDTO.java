package de.acosci.tasks.model.dto;

import de.acosci.tasks.model.enums.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Set;

/**
 * Request payload for creating a task.
 */
@Data
public class TaskCreateDTO {
    @NotNull
    private Long projectId;

    private Long boardColumnId;

    @NotBlank
    @Size(max = 255)
    private String title;

    @Size(max = 20000)
    private String description;

    private TaskPriority priority = TaskPriority.MEDIUM;
    private OffsetDateTime dueDate;
    private OffsetDateTime startAt;
    private Integer estimatedMinutes = 0;
    private String location;
    private Set<TaskCalendarReminderDTO> calendarReminders;
    private Set<Long> assigneeIds;
    private Set<Long> labelIds;
}
