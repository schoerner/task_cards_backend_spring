package de.acosci.tasks.controller.rest;

import de.acosci.tasks.model.dto.UserProfileResponseDTO;
import de.acosci.tasks.model.dto.UserProfileSummaryDTO;
import de.acosci.tasks.model.dto.UserProfileUpdateDTO;
import de.acosci.tasks.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "REST-API zur Verwaltung und Suche von Benutzerprofilen")
public class UserProfileRestController {

    private final UserProfileService userProfileService;

    @Operation(summary = "Eigenes Benutzerprofil abrufen")
    @GetMapping("/api/v1/users/me/profile")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<UserProfileResponseDTO> getOwnProfile() {
        return ResponseEntity.ok(userProfileService.getOwnProfile());
    }

    @Operation(summary = "Eigenes Benutzerprofil aktualisieren")
    @PutMapping("/api/v1/users/me/profile")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<UserProfileResponseDTO> updateOwnProfile(@Valid @RequestBody UserProfileUpdateDTO dto) {
        return ResponseEntity.ok(userProfileService.updateOwnProfile(dto));
    }

    @Operation(summary = "Benutzerprofile anhand von Anzeigename oder Kontakt-E-Mail suchen")
    @GetMapping("/api/v1/user-profiles")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<UserProfileSummaryDTO>> searchProfiles(@RequestParam(required = false) String query) {
        return ResponseEntity.ok(userProfileService.searchProfiles(query));
    }

    @Operation(summary = "Öffentlich sichtbares Benutzerprofil per Benutzer-ID abrufen")
    @GetMapping("/api/v1/user-profiles/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<UserProfileResponseDTO> getProfileByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(userProfileService.getProfileByUserId(userId));
    }
}
