package de.acosci.tasks.model.dto;

public record RegisterUserDTO(
        String email,
        String password,
        String firstName,
        String lastName
) {
}
