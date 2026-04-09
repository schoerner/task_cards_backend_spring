package de.acosci.tasks.model.dto;

import lombok.Data;

@Data
public class UserProfileSummaryDTO {
    private Long userId;
    private String name;
    private String contactEmail;
    private String pictureUrl;
}