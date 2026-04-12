package de.acosci.tasks.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BackupTriggerResponseDTO {
    private String status;
    private String message;
}