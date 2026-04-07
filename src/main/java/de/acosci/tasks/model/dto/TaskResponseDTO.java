package de.acosci.tasks.model.dto;

import de.acosci.tasks.model.enums.TaskPriority;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Set;

@Data
public class TaskResponseDTO {
    private Long id;
    private String title;
    private String description;
    private ProjectResponseDTO project;
    private BoardColumnResponseDTO boardColumn;
    private UserSummaryDTO creator;
    private TaskPriority priority;
    private OffsetDateTime dueDate;
    private boolean archived;
    private Integer estimatedMinutes;
    private Integer trackedMinutes;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Set<UserSummaryDTO> assignees;
    private Set<TaskLabelResponseDTO> labels;
    private boolean active;
}
