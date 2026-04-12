package de.acosci.tasks.model.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class BackupFileResponseDTO {
    private String fileName;
    private long sizeBytes;
    private Instant createdAt;
    private String retentionBucket;
    private boolean latest;
}