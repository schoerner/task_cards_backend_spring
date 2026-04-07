package de.acosci.tasks.service.impl;

import de.acosci.tasks.model.dto.TaskCommentCreateDTO;
import de.acosci.tasks.model.entity.Task;
import de.acosci.tasks.model.entity.TaskComment;
import de.acosci.tasks.model.entity.User;
import de.acosci.tasks.repository.TaskCommentRepository;
import de.acosci.tasks.repository.TaskRepository;
import de.acosci.tasks.repository.UserRepository;
import de.acosci.tasks.service.TaskCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskCommentServiceImpl implements TaskCommentService {

    private final TaskRepository taskRepository;
    private final TaskCommentRepository taskCommentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TaskComment> getCommentsByTask(Long taskId) {
        return taskCommentRepository.findAllByTask_IdOrderByCreatedAtAsc(taskId);
    }

    @Override
    public TaskComment createComment(Long taskId, TaskCommentCreateDTO dto) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));
        User currentUser = getCurrentUser();
        TaskComment comment = new TaskComment();
        comment.setTask(task);
        comment.setAuthor(currentUser);
        comment.setContent(dto.getContent());
        return taskCommentRepository.save(comment);
    }

    @Override
    public void deleteComment(Long taskId, Long commentId) {
        TaskComment comment = taskCommentRepository.findById(commentId)
                .filter(c -> c.getTask().getId().equals(taskId))
                .orElseThrow(() -> new IllegalArgumentException("Comment not found: " + commentId));
        taskCommentRepository.delete(comment);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + email));
    }
}
