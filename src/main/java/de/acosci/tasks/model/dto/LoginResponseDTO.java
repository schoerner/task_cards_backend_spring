package de.acosci.tasks.model.dto;

public record LoginResponseDTO(
        String token,
        Long expiresIn
) {
}
