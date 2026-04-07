package de.acosci.tasks.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request payload for creating a project.
 */
@Data
public class ProjectCreateDTO {
    @NotBlank
    @Size(max = 150)
    private String name;

    @Size(max = 5000)
    private String description;
}
