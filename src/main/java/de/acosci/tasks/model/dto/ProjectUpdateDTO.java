package de.acosci.tasks.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request payload for updating a project.
 */
@Data
public class ProjectUpdateDTO {
    @NotBlank
    @Size(max = 150)
    private String name;

    @Size(max = 5000)
    private String description;

    private boolean archived;
}
