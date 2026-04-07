package de.acosci.tasks.controller.rest;

import de.acosci.tasks.model.dto.UserProfileResponseDTO;
import de.acosci.tasks.model.dto.UserProfileUpdateDTO;
import de.acosci.tasks.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "REST-API zur Verwaltung des eigenen Benutzerprofils")
@RequestMapping("/api/v1/users/me/profile")
public class UserProfileRestController {

    private final UserProfileService userProfileService;

    @Operation(summary = "Eigenes Benutzerprofil abrufen")
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<UserProfileResponseDTO> getOwnProfile() {
        return ResponseEntity.ok(userProfileService.getOwnProfile());
    }

    @Operation(summary = "Eigenes Benutzerprofil aktualisieren")
    @PutMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<UserProfileResponseDTO> updateOwnProfile(@Valid @RequestBody UserProfileUpdateDTO dto) {
        return ResponseEntity.ok(userProfileService.updateOwnProfile(dto));
    }
}