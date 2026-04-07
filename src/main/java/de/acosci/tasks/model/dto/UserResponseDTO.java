package de.acosci.tasks.model.dto;

import lombok.Data;

import java.util.Date;
import java.util.Set;

@Data
public class UserResponseDTO {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private Date registration;
    private Set<String> roles;
    private UserProfileResponseDTO profile;
}