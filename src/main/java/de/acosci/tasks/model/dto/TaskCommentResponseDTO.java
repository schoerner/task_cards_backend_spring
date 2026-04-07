package de.acosci.tasks.model.dto;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class TaskCommentResponseDTO {
    private Long id;
    private Long taskId;
    private Long authorUserId;
    private String authorEmail;
    private String authorFirstName;
    private String authorLastName;
    private String content;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
