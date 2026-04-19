package de.acosci.tasks.model.dto;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class TaskPollHeatmapSlotDTO {
    private OffsetDateTime slotStartAt;
    private Integer availableCount;
    private Integer ifNeededCount;
    private Integer unavailableCount;
    private Integer score;
    private List<String> availableNames = new ArrayList<>();
    private List<String> ifNeededNames = new ArrayList<>();
    private List<String> unavailableNames = new ArrayList<>();
}
