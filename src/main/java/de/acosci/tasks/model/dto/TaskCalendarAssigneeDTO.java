package de.acosci.tasks.model.dto;

import lombok.Data;

@Data
public class TaskCalendarAssigneeDTO {
    private Long userId;
    private String email;
    private String profileName;
}