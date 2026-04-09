package de.acosci.tasks.service.impl;

import de.acosci.tasks.model.dto.BoardColumnCreateDTO;
import de.acosci.tasks.model.dto.BoardColumnUpdateDTO;
import de.acosci.tasks.model.entity.BoardColumn;
import de.acosci.tasks.model.entity.Project;
import de.acosci.tasks.model.entity.Task;
import de.acosci.tasks.repository.BoardColumnRepository;
import de.acosci.tasks.repository.ProjectRepository;
import de.acosci.tasks.repository.TaskRepository;
import de.acosci.tasks.service.BoardColumnService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class BoardColumnServiceImpl implements BoardColumnService {

    private final BoardColumnRepository boardColumnRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    @Override
    @Transactional(readOnly = true)
    public List<BoardColumn> getBoardColumns(Long projectId) {
        return boardColumnRepository.findAllByProject_IdOrderByPositionAsc(projectId);
    }

    @Override
    public BoardColumn createColumn(Long projectId, BoardColumnCreateDTO dto) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));
        BoardColumn column = new BoardColumn();
        column.setProject(project);
        column.setName(dto.getName());
        column.setPosition(dto.getPosition());
        return boardColumnRepository.save(column);
    }

    @Override
    public BoardColumn updateColumn(Long projectId, Long columnId, BoardColumnUpdateDTO dto) {
        BoardColumn column = getColumn(projectId, columnId);
        column.setName(dto.getName());
        column.setPosition(dto.getPosition());
        return boardColumnRepository.save(column);
    }

    @Override
    public List<BoardColumn> reorderColumns(Long projectId, List<Long> orderedColumnIds) {
        List<BoardColumn> existingColumns = boardColumnRepository.findAllByProject_IdOrderByPositionAsc(projectId);
        if (existingColumns.isEmpty()) {
            throw new IllegalArgumentException("No board columns found for project: " + projectId);
        }

        Set<Long> existingIds = existingColumns.stream().map(BoardColumn::getId).collect(java.util.stream.Collectors.toSet());
        Set<Long> requestedIds = new HashSet<>(orderedColumnIds);

        if (orderedColumnIds.size() != existingColumns.size() || requestedIds.size() != existingColumns.size() || !existingIds.equals(requestedIds)) {
            throw new IllegalArgumentException("orderedColumnIds must contain each project board column exactly once.");
        }

        for (int index = 0; index < orderedColumnIds.size(); index++) {
            Long columnId = orderedColumnIds.get(index);
            BoardColumn column = existingColumns.stream()
                    .filter(it -> it.getId().equals(columnId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Board column not found: " + columnId));
            column.setPosition(index);
        }

        boardColumnRepository.saveAll(existingColumns);
        return boardColumnRepository.findAllByProject_IdOrderByPositionAsc(projectId);
    }

    @Override
    public void deleteColumn(Long projectId, Long columnId, Long fallbackColumnId) {
        BoardColumn column = getColumn(projectId, columnId);
        if (!column.isDeletable()) {
            throw new IllegalStateException("Protected board columns cannot be deleted.");
        }
        BoardColumn fallback = getColumn(projectId, fallbackColumnId);
        List<Task> tasks = taskRepository.findAllByProject_IdAndBoardColumn_IdAndArchivedFalse(projectId, columnId);
        tasks.forEach(task -> task.setBoardColumn(fallback));
        boardColumnRepository.delete(column);
    }

    private BoardColumn getColumn(Long projectId, Long columnId) {
        return boardColumnRepository.findById(columnId)
                .filter(column -> column.getProject().getId().equals(projectId))
                .orElseThrow(() -> new IllegalArgumentException("Board column not found: " + columnId));
    }
}
