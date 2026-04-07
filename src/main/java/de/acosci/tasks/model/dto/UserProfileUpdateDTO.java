package de.acosci.tasks.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserProfileUpdateDTO {

    @Size(max = 100)
    private String name;

    @Email
    @Size(max = 255)
    private String contactEmail;

    @Size(max = 1000)
    private String pictureUrl;

    @Size(max = 2000)
    private String description;
}