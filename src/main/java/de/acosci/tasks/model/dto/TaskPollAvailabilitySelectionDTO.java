package de.acosci.tasks.model.dto;

import de.acosci.tasks.model.enums.TaskPollAvailabilityStatus;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class TaskPollAvailabilitySelectionDTO {
    private OffsetDateTime slotStartAt;
    private TaskPollAvailabilityStatus availability;
}
