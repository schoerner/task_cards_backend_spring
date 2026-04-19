package de.acosci.tasks.service;

import de.acosci.tasks.model.dto.TaskPollMailRequestDTO;

public interface TaskPollMailService {
    void sendInvitations(Long taskId, TaskPollMailRequestDTO dto);
    void sendReminders(Long taskId, TaskPollMailRequestDTO dto);
    void sendFinalizationNotification(Long taskId, TaskPollMailRequestDTO dto);
}
