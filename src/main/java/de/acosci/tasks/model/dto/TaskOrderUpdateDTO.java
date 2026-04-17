package de.acosci.tasks.model.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TaskOrderUpdateDTO {
    private List<Long> taskIds = new ArrayList<>();
}