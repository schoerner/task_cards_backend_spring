package de.acosci.tasks.model.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BoardColumnReorderDTO {
    @NotEmpty
    private List<@NotNull Long> orderedColumnIds;
}
