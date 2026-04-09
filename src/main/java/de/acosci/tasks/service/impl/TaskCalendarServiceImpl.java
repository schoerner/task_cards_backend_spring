package de.acosci.tasks.service.impl;

import de.acosci.tasks.model.dto.TaskCalendarAssigneeDTO;
import de.acosci.tasks.model.dto.TaskCalendarEntryDTO;
import de.acosci.tasks.model.dto.TaskCalendarReminderDTO;
import de.acosci.tasks.model.entity.Task;
import de.acosci.tasks.model.entity.User;
import de.acosci.tasks.repository.TaskRepository;
import de.acosci.tasks.repository.UserRepository;
import de.acosci.tasks.service.TaskCalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskCalendarServiceImpl implements TaskCalendarService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Override
    public List<TaskCalendarEntryDTO> getCalendarTasksForCurrentUser() {
        User currentUser = getCurrentUser();
        return getCalendarTasksForUser(currentUser.getId());
    }

    @Override
    public List<TaskCalendarEntryDTO> getCalendarTasksForUser(Long userId) {
        return taskRepository.findCalendarTasksForUser(userId)
                .stream()
                .map(this::toCalendarEntryDTO)
                .toList();
    }

    private TaskCalendarEntryDTO toCalendarEntryDTO(Task task) {
        TaskCalendarEntryDTO dto = new TaskCalendarEntryDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setStartAt(task.getStartAt());
        dto.setDueDate(task.getDueDate());
        dto.setLocation(task.getLocation());
        dto.setDescription(task.getDescription());
        dto.setArchived(task.isArchived());
        dto.setActive(task.isActive());

        dto.setProjectId(task.getProject() != null ? task.getProject().getId() : null);
        dto.setProjectName(task.getProject() != null ? task.getProject().getName() : null);

        dto.setBoardColumnId(task.getBoardColumn() != null ? task.getBoardColumn().getId() : null);
        dto.setBoardColumnName(task.getBoardColumn() != null ? task.getBoardColumn().getName() : null);

        Set<TaskCalendarReminderDTO> reminderDTOs = new LinkedHashSet<>();
        if (task.getCalendarReminders() != null) {
            task.getCalendarReminders().forEach(reminder -> {
                TaskCalendarReminderDTO reminderDTO = new TaskCalendarReminderDTO();
                reminderDTO.setId(reminder.getId());
                reminderDTO.setMinutesBefore(reminder.getMinutesBefore());
                reminderDTO.setActionType(reminder.getActionType());
                reminderDTO.setMessage(reminder.getMessage());
                reminderDTOs.add(reminderDTO);
            });
        }
        dto.setCalendarReminders(reminderDTOs);
        
        Set<TaskCalendarAssigneeDTO> assigneeDTOs = new LinkedHashSet<>();
        for (User assignee : task.getAssignees()) {
            TaskCalendarAssigneeDTO assigneeDTO = new TaskCalendarAssigneeDTO();
            assigneeDTO.setUserId(assignee.getId());
            assigneeDTO.setEmail(assignee.getEmail());
            assigneeDTO.setProfileName(
                    assignee.getProfile() != null ? assignee.getProfile().getName() : null
            );
            assigneeDTOs.add(assigneeDTO);
        }
        dto.setAssignees(assigneeDTOs);

        return dto;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + email));
    }
}