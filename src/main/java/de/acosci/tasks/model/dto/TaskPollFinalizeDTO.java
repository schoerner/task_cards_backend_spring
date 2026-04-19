package de.acosci.tasks.model.dto;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class TaskPollFinalizeDTO {
    private OffsetDateTime finalizedStartAt;
    private OffsetDateTime finalizedEndAt;
}
