package de.acosci.tasks.security;

import de.acosci.tasks.repository.TaskRepository;
import de.acosci.tasks.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Task-specific authorization helper delegating to project security.
 */
@Component("taskSecurity")
@RequiredArgsConstructor
public class TaskSecurity {

    private final TaskRepository taskRepository;
    private final ProjectSecurity projectSecurity;
    private final UserRepository userRepository;

    public boolean canViewTask(Long taskId, Long userId) {
        return taskRepository.findById(taskId)
                .map(task -> projectSecurity.isMember(task.getProject().getId(), userId))
                .orElse(false);
    }

    public boolean canViewTaskByEmail(Long taskId, String email) {
        return userRepository.findByEmail(email)
                .map(user -> canViewTask(taskId, user.getId()))
                .orElse(false);
    }

    public boolean canEditTask(Long taskId, Long userId) {
        return taskRepository.findById(taskId)
                .map(task -> projectSecurity.canEditTasks(task.getProject().getId(), userId))
                .orElse(false);
    }

    public boolean canEditTaskByEmail(Long taskId, String email) {
        return userRepository.findByEmail(email)
                .map(user -> canEditTask(taskId, user.getId()))
                .orElse(false);
    }
}
