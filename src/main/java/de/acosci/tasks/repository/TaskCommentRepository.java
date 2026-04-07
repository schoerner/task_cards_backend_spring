package de.acosci.tasks.repository;

import de.acosci.tasks.model.entity.TaskComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskCommentRepository extends JpaRepository<TaskComment, Long> {
    List<TaskComment> findAllByTask_IdOrderByCreatedAtAsc(Long taskId);
}
