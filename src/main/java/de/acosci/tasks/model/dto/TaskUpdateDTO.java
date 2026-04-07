package de.acosci.tasks.model.dto;

import de.acosci.tasks.model.enums.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Set;

/**
 * Request payload for updating a task.
 */
@Data
public class TaskUpdateDTO {
    @NotBlank
    @Size(max = 255)
    private String title;

    @Size(max = 20000)
    private String description;

    private Long boardColumnId;
    private TaskPriority priority;
    private OffsetDateTime dueDate;
    private Integer estimatedMinutes;
    private Integer trackedMinutes;
    private boolean archived;
    private Set<Long> assigneeIds;
    private Set<Long> labelIds;
}
