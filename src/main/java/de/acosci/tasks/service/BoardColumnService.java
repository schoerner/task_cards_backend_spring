package de.acosci.tasks.service;

import de.acosci.tasks.model.dto.BoardColumnCreateDTO;
import de.acosci.tasks.model.dto.BoardColumnUpdateDTO;
import de.acosci.tasks.model.entity.BoardColumn;

import java.util.List;

/**
 * Service for managing board columns.
 */
public interface BoardColumnService {
    List<BoardColumn> getBoardColumns(Long projectId);
    BoardColumn createColumn(Long projectId, BoardColumnCreateDTO dto);
    BoardColumn updateColumn(Long projectId, Long columnId, BoardColumnUpdateDTO dto);
    List<BoardColumn> reorderColumns(Long projectId, List<Long> orderedColumnIds);
    void deleteColumn(Long projectId, Long columnId, Long fallbackColumnId);
}
