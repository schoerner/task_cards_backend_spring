package de.acosci.tasks.model.dto;

import lombok.Data;

@Data
public class UserProfileResponseDTO {
    private Long userId;
    private String name;
    private String contactEmail;
    private String pictureUrl;
    private String description;
}