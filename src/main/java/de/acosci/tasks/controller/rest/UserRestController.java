package de.acosci.tasks.controller.rest;

import de.acosci.tasks.model.dto.ChangePasswordDTO;
import de.acosci.tasks.model.dto.UserResponseDTO;
import de.acosci.tasks.model.dto.UserUpdateDTO;
import de.acosci.tasks.model.mapper.UserMapper;
import de.acosci.tasks.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "REST-API für Benutzeraktionen des eigenen Accounts")
public class UserRestController {

    private final UserService userService;

    @Operation(summary = "Benutzer anhand der ID abrufen", description = "Erlaubt nur für den Benutzer selbst oder Administratoren.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Benutzer erfolgreich gefunden",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Zugriff verboten", content = @Content),
            @ApiResponse(responseCode = "404", description = "Benutzer nicht gefunden", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isSelf(#id)")
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(
            @Parameter(description = "ID des Benutzers", example = "1", required = true)
            @PathVariable long id) {
        try {
            return ResponseEntity.ok(UserMapper.toUserResponseDTO(userService.getUserByID(id)));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Eigenen Benutzer aktualisieren", description = "Erlaubt nur für den Benutzer selbst oder Administratoren.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Benutzer erfolgreich aktualisiert",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Zugriff verboten", content = @Content),
            @ApiResponse(responseCode = "404", description = "Benutzer nicht gefunden", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isSelf(#id)")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUserById(
            @Parameter(description = "ID des zu aktualisierenden Benutzers", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateDTO dto) {
        try {
            return ResponseEntity.ok(UserMapper.toUserResponseDTO(userService.updateUser(id, dto)));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Eigenen Benutzer löschen", description = "Erlaubt nur für den Benutzer selbst oder Administratoren.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Benutzer erfolgreich gelöscht", content = @Content),
            @ApiResponse(responseCode = "403", description = "Zugriff verboten", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isSelf(#id)")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUserById(
            @Parameter(description = "ID des zu löschenden Benutzers", example = "1", required = true)
            @PathVariable Long id) {
        userService.deleteUserByID(id);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Eigenes Passwort ändern",
            description = "Erlaubt nur für den Benutzer selbst oder Administratoren. "
                    + "Prüft das aktuelle Passwort und setzt anschließend ein neues Passwort."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Passwort erfolgreich geändert", content = @Content),
            @ApiResponse(responseCode = "400", description = "Ungültige Eingabedaten oder aktuelles Passwort falsch", content = @Content),
            @ApiResponse(responseCode = "403", description = "Zugriff verboten", content = @Content),
            @ApiResponse(responseCode = "404", description = "Benutzer nicht gefunden", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isSelf(#id)")
    @PatchMapping("/{id}/password")
    public ResponseEntity<?> changePassword(
            @Parameter(description = "ID des Benutzers", example = "1", required = true)
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Aktuelles Passwort sowie neues Passwort und Wiederholung",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ChangePasswordDTO.class))
            )
            @Valid @RequestBody ChangePasswordDTO dto) {
        try {
            userService.changePassword(id, dto);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}