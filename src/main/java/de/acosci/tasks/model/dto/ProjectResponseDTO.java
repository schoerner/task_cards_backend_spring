package de.acosci.tasks.model.dto;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class ProjectResponseDTO {
    private Long id;
    private String name;
    private String description;
    private boolean archived;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}