package de.acosci.tasks.model.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class TaskPollPublicResponseDTO {
    private Long participantId;
    private String displayName;
    private Long taskId;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime dayStartTime;
    private LocalTime dayEndTime;
    private Integer slotMinutes;
    private List<LocalDate> includedDates = new ArrayList<>();
    private List<TaskPollAvailabilitySelectionDTO> response = new ArrayList<>();
    private List<TaskPollHeatmapSlotDTO> heatmap;
}
