package de.acosci.tasks.model.dto;

import lombok.Data;

@Data
public class TaskCalendarReminderDTO {
    private Long id;
    private Integer minutesBefore;
    private String actionType;
    private String message;
}