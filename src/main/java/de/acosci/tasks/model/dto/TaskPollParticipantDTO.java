package de.acosci.tasks.model.dto;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class TaskPollParticipantDTO {
    private Long id;
    private String type;
    private Long userId;
    private String displayName;
    private String email;
    private String responseName;
    private OffsetDateTime invitedAt;
    private OffsetDateTime respondedAt;
    private OffsetDateTime lastReminderAt;
    private List<TaskPollAvailabilitySelectionDTO> response = new ArrayList<>();
}
