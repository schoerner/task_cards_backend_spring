package de.acosci.tasks.repository;

import de.acosci.tasks.model.entity.TaskActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskActivityRepository extends JpaRepository<TaskActivity, Long> {
    List<TaskActivity> findAllByTask_IdOrderByCreatedAtAsc(Long taskId);
}
