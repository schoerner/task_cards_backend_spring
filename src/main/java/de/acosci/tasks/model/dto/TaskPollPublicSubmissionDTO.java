package de.acosci.tasks.model.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TaskPollPublicSubmissionDTO {
    private String displayName;
    private List<TaskPollAvailabilitySelectionDTO> response = new ArrayList<>();
}
