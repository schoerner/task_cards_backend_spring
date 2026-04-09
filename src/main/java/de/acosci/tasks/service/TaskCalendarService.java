package de.acosci.tasks.service;

import de.acosci.tasks.model.dto.TaskCalendarEntryDTO;

import java.util.List;

public interface TaskCalendarService {
    List<TaskCalendarEntryDTO> getCalendarTasksForCurrentUser();
    List<TaskCalendarEntryDTO> getCalendarTasksForUser(Long userId);
}