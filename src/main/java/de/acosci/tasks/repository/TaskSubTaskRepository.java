package de.acosci.tasks.repository;

import de.acosci.tasks.model.entity.TaskSubTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskSubTaskRepository extends JpaRepository<TaskSubTask, Long> {
    List<TaskSubTask> findAllByTask_IdOrderByPositionAsc(Long taskId);
}
