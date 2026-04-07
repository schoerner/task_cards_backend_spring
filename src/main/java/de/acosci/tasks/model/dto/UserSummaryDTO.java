package de.acosci.tasks.model.dto;

import lombok.Data;

@Data
public class UserSummaryDTO {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
}
