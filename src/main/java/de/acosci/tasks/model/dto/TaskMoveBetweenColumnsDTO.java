package de.acosci.tasks.model.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TaskMoveBetweenColumnsDTO {
    private Long sourceColumnId;
    private Long targetColumnId;
    private List<Long> sourceTaskIds = new ArrayList<>();
    private List<Long> targetTaskIds = new ArrayList<>();
}