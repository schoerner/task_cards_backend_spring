package de.acosci.tasks.service;

public interface TaskPollMailService {
    void sendInvitations(Long taskId);
    void sendReminders(Long taskId);
}