package de.acosci.tasks.controller.rest;

import de.acosci.tasks.model.dto.UserManagementCreateDTO;
import de.acosci.tasks.model.dto.UserManagementUpdateDTO;
import de.acosci.tasks.model.dto.UserResponseDTO;
import de.acosci.tasks.service.UserManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/user-management")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "User Management", description = "REST-API zur administrativen Benutzerverwaltung")
public class UserManagementRestController {

    private final UserManagementService userManagementService;

    @Operation(summary = "Alle Benutzer abrufen")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Benutzerliste erfolgreich geladen",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserResponseDTO.class)))
            ),
            @ApiResponse(responseCode = "403", description = "Zugriff verboten", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userManagementService.getUsers());
    }

    @Operation(summary = "Benutzer anhand der ID abrufen")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Benutzer erfolgreich gefunden",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))
            ),
            @ApiResponse(responseCode = "404", description = "Benutzer nicht gefunden", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(
            @Parameter(description = "ID des Benutzers", example = "1", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(userManagementService.getUserById(id));
    }

    @Operation(summary = "Neuen Benutzer anlegen")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Benutzer erfolgreich angelegt",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Ungültige Eingabedaten", content = @Content)
    })
    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserManagementCreateDTO dto) {
        return new ResponseEntity<>(userManagementService.createUser(dto), HttpStatus.CREATED);
    }

    @Operation(summary = "Benutzer aktualisieren")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Benutzer erfolgreich aktualisiert",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Ungültige Eingabedaten", content = @Content),
            @ApiResponse(responseCode = "404", description = "Benutzer nicht gefunden", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @Parameter(description = "ID des zu aktualisierenden Benutzers", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UserManagementUpdateDTO dto) {
        return ResponseEntity.ok(userManagementService.updateUser(id, dto));
    }

    @Operation(summary = "Benutzer löschen")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Benutzer erfolgreich gelöscht", content = @Content),
            @ApiResponse(responseCode = "404", description = "Benutzer nicht gefunden", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID des zu löschenden Benutzers", example = "1", required = true)
            @PathVariable Long id) {
        userManagementService.deleteUserById(id);
        return ResponseEntity.ok().build();
    }
}