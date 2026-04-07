package de.acosci.tasks.model.dto;

import de.acosci.tasks.model.enums.BoardColumnType;
import lombok.Data;

@Data
public class BoardColumnResponseDTO {
    private Long id;
    private Long projectId;
    private String name;
    private Integer position;
    private BoardColumnType type;
    private boolean deletable;
}
