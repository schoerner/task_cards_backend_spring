package de.acosci.tasks.model.dto;

public record TaskTimeDTO(
        Long id,
        String title,
        String description,
        Boolean active,
        TimeRecordDTO activeTimeRecordDTO
) {
}
