package de.acosci.tasks.model.dto;

import de.acosci.tasks.model.enums.TaskPriority;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Data
public class TaskResponseDTO {
    private Long id;
    private String title;
    private String description;
    private Long projectId;
    private Long boardColumnId;
    private Long creatorId;
    private ProjectSummaryDTO project;
    private BoardColumnSummaryDTO boardColumn;
    private TaskPriority priority;
    private OffsetDateTime startAt;
    private OffsetDateTime dueDate;
    private String location;
    private boolean archived;
    private Integer estimatedMinutes;
    private Integer trackedMinutes;
    private boolean active;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Set<TaskCalendarReminderDTO> calendarReminders = new LinkedHashSet<>();
    private Set<UserSummaryDTO> assignees = new LinkedHashSet<>();
    private Set<TaskLabelResponseDTO> labels = new LinkedHashSet<>();
}
