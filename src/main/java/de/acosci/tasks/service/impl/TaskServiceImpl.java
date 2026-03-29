package de.acosci.tasks.service.impl;

import de.acosci.tasks.model.entity.Role.RoleName;
import de.acosci.tasks.model.entity.Task;
import de.acosci.tasks.model.entity.TimeRecord;
import de.acosci.tasks.model.entity.User;
import de.acosci.tasks.repository.TaskRepository;
import de.acosci.tasks.repository.TimeRecordRepository;
import de.acosci.tasks.repository.UserRepository;
import de.acosci.tasks.service.TaskService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TimeRecordRepository timeRecordRepository;

    // 🔹 Hilfsmethoden ------------------------------------------------------

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream().anyMatch(r -> r.getName() == RoleName.ROLE_ADMIN);
    }

    private boolean isModerator(User user) {
        return user.getRoles().stream().anyMatch(r -> r.getName() == RoleName.ROLE_MODERATOR);
    }

    private boolean isOwner(Task task, User user) {
        return task.getCreator() != null && task.getCreator().getId().equals(user.getId());
    }

    private boolean isAssignee(Task task, User user) {
        return task.getAssignees().stream().anyMatch(u -> u.getId().equals(user.getId()));
    }

    private void ensureCanView(Task task, User user) {
        if (isAdmin(user)) return;
        if (isOwner(task, user)) return;
        if (isAssignee(task, user)) return;
        throw new AccessDeniedException("User cannot view this task");
    }

    private void ensureCanModify(Task task, User user) {
        if (isAdmin(user)) return;
        if (isOwner(task, user) && isModerator(user)) return;
        throw new AccessDeniedException("User cannot modify this task");
    }

    private List<Task> determineActiveTasks(List<Task> tasks) {
        tasks.forEach(task -> {
            boolean active = task.getTimeRecords().stream()
                    .anyMatch(timeRecord -> timeRecord.getTimeEnd() == null);
            task.setActive(active);
        });
        return tasks;
    }

    // 🔹 Service-Methoden ------------------------------------------------------

    @Override
    @PreAuthorize("isAuthenticated()")
    public List<Task> getAllTasks() {
        User currentUser = getCurrentUser();
        if (isAdmin(currentUser)) {
            return determineActiveTasks(taskRepository.findAll());
        }
        return determineActiveTasks(
                taskRepository.findAll().stream()
                        .filter(task -> isOwner(task, currentUser) || isAssignee(task, currentUser))
                        .collect(Collectors.toList())
        );
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public List<Task> getMyTasks() {
        User currentUser = getCurrentUser();
        return determineActiveTasks(
                taskRepository.findAll().stream()
                        .filter(task -> isOwner(task, currentUser) || isAssignee(task, currentUser))
                        .collect(Collectors.toList())
        );
    }

    @Override
    @PreAuthorize("hasAnyRole('MODERATOR','ADMIN')")
    public List<Task> getMyOwnedTasks() {
        User currentUser = getCurrentUser();
        return determineActiveTasks(taskRepository.findByCreator_Id(currentUser.getId()));
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public Task getTaskByID(Long id) {
        User currentUser = getCurrentUser();
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id " + id));
        ensureCanView(task, currentUser);
        return task;
    }

    @Override
    @PreAuthorize("hasAnyRole('MODERATOR','ADMIN')")
    public Task saveTask(Task task) {
        User currentUser = getCurrentUser();
        task.setCreator(currentUser);
        return taskRepository.save(task);
    }

    @PreAuthorize("hasAnyRole('MODERATOR','ADMIN')")
    @Override
    public Task updateTask(Task task) {
        User currentUser = getCurrentUser();
        Task existing = taskRepository.findById(task.getId())
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id " + task.getId()));
        ensureCanModify(existing, currentUser);

        existing.setTitle(task.getTitle());
        existing.setDescription(task.getDescription());
        existing.setProcessInPercentage(task.getProcessInPercentage());
        existing.setCompleted(task.getCompleted());
        existing.setAssignees(task.getAssignees());
        existing.setProject(task.getProject());

        return taskRepository.save(existing);
    }

    @PreAuthorize("hasAnyRole('MODERATOR','ADMIN')")
    @Override
    public Task patchTask(Long id, Map<String, Object> updates) {
        User currentUser = getCurrentUser();

        Task existing = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id " + id));

        ensureCanModify(existing, currentUser);

        if (updates == null || updates.isEmpty()) {
            throw new IllegalArgumentException("No updates provided");
        }

        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            String field = entry.getKey();
            Object value = entry.getValue();

            switch (field) {

                case "title" -> {
                    if (value == null) {
                        existing.setTitle(null);
                    } else if (value instanceof String s) {
                        existing.setTitle(s);
                    } else {
                        throw new IllegalArgumentException("title must be a string");
                    }
                }

                case "description" -> {
                    if (value == null) {
                        existing.setDescription(null);
                    } else if (value instanceof String s) {
                        existing.setDescription(s);
                    } else {
                        throw new IllegalArgumentException("description must be a string");
                    }
                }

                case "processInPercentage" -> {
                    if (value == null) {
                        existing.setProcessInPercentage(null); // falls Integer/Wrapper; sonst anpassen
                    } else if (value instanceof Double n) {
                        double p = n.doubleValue();
                        if (p < 0 || p > 100) {
                            throw new IllegalArgumentException("processInPercentage must be between 0 and 100");
                        }
                        existing.setProcessInPercentage(p);
                    } else {
                        throw new IllegalArgumentException("processInPercentage must be a number");
                    }
                }

                case "completed" -> {
                    if (value == null) {
                        existing.setCompleted(null); // falls Boolean/Wrapper; sonst anpassen
                    } else if (value instanceof Boolean b) {
                        existing.setCompleted(b);
                    } else {
                        throw new IllegalArgumentException("completed must be boolean");
                    }
                }

                /*
                 * Optional: assignees als Liste von User-IDs patchen.
                 * Beispiel-JSON: { "assignees": [3, 7, 9] }
                 */
                case "assignees" -> {
                    if (value == null) {
                        existing.setAssignees(new HashSet<>());
                    } else if (value instanceof java.util.List<?> rawList) {
                        List<Long> ids = new ArrayList<>();
                        for (Object o : rawList) {
                            if (o instanceof Number n) {
                                ids.add(n.longValue());
                            } else {
                                throw new IllegalArgumentException("assignees must be an array of numeric user ids");
                            }
                        }
                        Set<User> users = new HashSet<>(userRepository.findAllById(ids));
                        if (users.size() != ids.size()) {
                            throw new IllegalArgumentException("assignees contains unknown user ids");
                        }
                        existing.setAssignees(users);
                    } else {
                        throw new IllegalArgumentException("assignees must be an array");
                    }
                }

                // Felder, die NICHT gepatcht werden dürfen
                case "id", "creator", "timeRecords", "active", "project" -> {
                    throw new IllegalArgumentException("Field '" + field + "' cannot be patched");
                }

                // Unbekannte Felder
                default -> throw new IllegalArgumentException("Unknown field: " + field);
            }
        }

        return taskRepository.save(existing);
    }

    @Override
    @PreAuthorize("hasAnyRole('MODERATOR','ADMIN')")
    public void deleteTaskByID(Long id) {
        User currentUser = getCurrentUser();
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id " + id));
        ensureCanModify(task, currentUser);
        taskRepository.deleteById(id);
    }

    @Override
    @PreAuthorize("hasAnyRole('MODERATOR','ADMIN')")
    public void deleteTask(Task task) {
        deleteTaskByID(task.getId());
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public Task startTask(Long taskID) {
        User currentUser = getCurrentUser();
        Task task = taskRepository.findById(taskID)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id " + taskID));
        ensureCanView(task, currentUser);

        TimeRecord newTimeRecord = new TimeRecord(task);
        task.getTimeRecords().add(newTimeRecord);

        timeRecordRepository.save(newTimeRecord);
        Task startedTask = taskRepository.save(task);
        startedTask.setActive(true);

        return startedTask;
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public Task stopTask(Long taskID) {
        User currentUser = getCurrentUser();
        Task task = taskRepository.findById(taskID)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id " + taskID));
        ensureCanView(task, currentUser);

        TimeRecord activeTimeRecord = getActiveTimeRecord(task);
        if (activeTimeRecord == null) {
            throw new IllegalStateException("No active time record found for task " + taskID);
        }

        activeTimeRecord.setTimeEnd(new Date());
        task.setActive(false);

        timeRecordRepository.save(activeTimeRecord);
        return taskRepository.save(task);
    }

    // 🔹 Hilfsmethoden für Zeitstatus ------------------------------

    @Override
    @PreAuthorize("isAuthenticated()")
    public List<Task> getActiveTasks() {
        return getAllTasks().stream()
                .filter(Task::isActive)
                .collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public TimeRecord getActiveTimeRecord(Long taskID) {
        Task task = getTaskByID(taskID);
        return getActiveTimeRecord(task);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public TimeRecord getActiveTimeRecord(Task task) {
        return task.getTimeRecords().stream()
                .filter(timeRecord -> timeRecord.getTimeEnd() == null)
                .findFirst().orElse(null);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public Boolean isActive(Long taskID) {
        return getActiveTimeRecord(taskID) != null;
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public Boolean isActive(Task task) {
        return getActiveTimeRecord(task) != null;
    }
}
