package de.acosci.tasks.service;

import de.acosci.tasks.model.dto.TaskCommentCreateDTO;
import de.acosci.tasks.model.entity.TaskComment;

import java.util.List;

/**
 * Service for task comments.
 */
public interface TaskCommentService {
    List<TaskComment> getCommentsByTask(Long taskId);
    TaskComment createComment(Long taskId, TaskCommentCreateDTO dto);
    void deleteComment(Long taskId, Long commentId);
}
