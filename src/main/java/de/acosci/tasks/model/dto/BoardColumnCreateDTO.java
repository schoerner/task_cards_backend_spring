package de.acosci.tasks.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request payload for creating a board column.
 */
@Data
public class BoardColumnCreateDTO {
    @NotBlank
    @Size(max = 100)
    private String name;

    @Min(0)
    @Max(999)
    private Integer position;
}
