package de.acosci.tasks.repository;

import de.acosci.tasks.model.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllByProject_IdAndArchivedFalse(Long projectId);
    List<Task> findAllByProject_IdAndBoardColumn_IdAndArchivedFalse(Long projectId, Long boardColumnId);
    List<Task> findAllByAssignees_IdAndArchivedFalse(Long assigneeUserId);
}
