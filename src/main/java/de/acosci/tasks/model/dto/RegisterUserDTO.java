package de.acosci.tasks.model.dto;

public record RegisterUserDTO(
        String email,
        String password,
        String passwordVerification,
        String firstName,
        String lastName
) {
}
