package de.acosci.tasks.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request payload for creating a task comment.
 */
@Data
public class TaskCommentCreateDTO {
    @NotBlank
    @Size(max = 10000)
    private String content;
}
