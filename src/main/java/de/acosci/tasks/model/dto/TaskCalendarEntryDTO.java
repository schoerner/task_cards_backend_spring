package de.acosci.tasks.model.dto;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Data
public class TaskCalendarEntryDTO {
    private Long id;
    private String title;
    private String description;
    private OffsetDateTime startAt;
    private OffsetDateTime dueDate;
    private String location;
    private boolean archived;
    private boolean active;

    private Long projectId;
    private String projectName;

    private Long boardColumnId;
    private String boardColumnName;

    private Set<TaskCalendarReminderDTO> calendarReminders = new LinkedHashSet<>();
    private Set<TaskCalendarAssigneeDTO> assignees = new LinkedHashSet<>();
}