package de.acosci.tasks.repository;

import de.acosci.tasks.model.entity.TaskDependency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskDependencyRepository extends JpaRepository<TaskDependency, Long> {
    List<TaskDependency> findAllByBlockingTask_Id(Long taskId);
    List<TaskDependency> findAllByBlockedTask_Id(Long taskId);
    boolean existsByBlockingTask_IdAndBlockedTask_Id(Long blockingTaskId, Long blockedTaskId);
}
