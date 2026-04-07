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

import java.util.List;

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
