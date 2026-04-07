package de.acosci.tasks.model.mapper;

import de.acosci.tasks.model.dto.BoardColumnResponseDTO;
import de.acosci.tasks.model.entity.BoardColumn;

public final class BoardColumnMapper {

    private BoardColumnMapper() {
    }

    public static BoardColumnResponseDTO toResponseDTO(BoardColumn column) {
        if (column == null) {
            return null;
        }

        BoardColumnResponseDTO dto = new BoardColumnResponseDTO();
        dto.setId(column.getId());
        dto.setProjectId(column.getProject() != null ? column.getProject().getId() : null);
        dto.setName(column.getName());
        dto.setPosition(column.getPosition());
        dto.setType(column.getType());
        dto.setDeletable(column.isDeletable());
        return dto;
    }
}
