package de.acosci.tasks.model.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class TaskPollResponseDTO {
    private Long id;
    private Long taskId;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime dayStartTime;
    private LocalTime dayEndTime;
    private Integer slotMinutes;
    private List<LocalDate> includedDates = new ArrayList<>();
    private List<TaskPollParticipantDTO> participants = new ArrayList<>();
    private List<TaskPollAvailabilitySelectionDTO> ownerResponse = new ArrayList<>();
    private List<TaskPollHeatmapSlotDTO> heatmap = new ArrayList<>();
    private OffsetDateTime finalizedStartAt;
    private OffsetDateTime finalizedEndAt;
}
