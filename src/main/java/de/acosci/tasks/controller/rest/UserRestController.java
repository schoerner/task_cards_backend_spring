package de.acosci.tasks.controller.rest;

import de.acosci.tasks.model.entity.User;
import de.acosci.tasks.service.impl.UserServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost", "http://localhost:5173/", "http://localhost:3000/"})
@Tag(name = "Users", description = "REST-API zur Verwaltung von Benutzern")
public class UserRestController {

    private final UserServiceImpl userService;

    @Operation(
            summary = "Alle Benutzer abrufen",
            description = "Liefert eine Liste aller Benutzer. Dieser Endpunkt ist nur für Administratoren verfügbar."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Benutzerliste erfolgreich geladen",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = User.class)))
            ),
            @ApiResponse(responseCode = "400", description = "Fehler beim Laden der Benutzerliste", content = @Content),
            @ApiResponse(responseCode = "403", description = "Zugriff verboten", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        try {
            return new ResponseEntity<>(userService.getUsers(), HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(
            summary = "Benutzer anhand der ID abrufen",
            description = "Liefert einen einzelnen Benutzer anhand seiner ID. Erlaubt für Administratoren oder den Benutzer selbst."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Benutzer erfolgreich gefunden",
                    content = @Content(schema = @Schema(implementation = User.class))
            ),
            @ApiResponse(responseCode = "403", description = "Zugriff verboten", content = @Content),
            @ApiResponse(responseCode = "404", description = "Benutzer nicht gefunden", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isSelf(#id)")
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(
            @Parameter(description = "ID des Benutzers", example = "1", required = true)
            @PathVariable long id) {
        try {
            return new ResponseEntity<User>(userService.getUserByID(id), HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Neuen Benutzer anlegen",
            description = "Legt einen neuen Benutzer an. Dieser Endpunkt ist nur für Administratoren verfügbar."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Benutzer erfolgreich angelegt",
                    content = @Content(schema = @Schema(implementation = User.class))
            ),
            @ApiResponse(responseCode = "400", description = "Ungültige Eingabedaten", content = @Content),
            @ApiResponse(responseCode = "403", description = "Zugriff verboten", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<User> createUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Der anzulegende Benutzer",
                    required = true,
                    content = @Content(schema = @Schema(implementation = User.class))
            )
            @RequestBody User user) {
        try {
            return new ResponseEntity<User>(userService.saveUser(user), HttpStatus.CREATED);
        } catch (Exception e) { // todo
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(
            summary = "Benutzer aktualisieren",
            description = "Aktualisiert einen bestehenden Benutzer anhand seiner ID. Erlaubt für Administratoren oder den Benutzer selbst."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Benutzer erfolgreich aktualisiert",
                    content = @Content(schema = @Schema(implementation = User.class))
            ),
            @ApiResponse(responseCode = "400", description = "Ungültige Eingabedaten", content = @Content),
            @ApiResponse(responseCode = "403", description = "Zugriff verboten", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isSelf(#id)")
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUserByID(
            @Parameter(description = "ID des zu aktualisierenden Benutzers", example = "1", required = true)
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Die neuen Benutzerdaten",
                    required = true,
                    content = @Content(schema = @Schema(implementation = User.class))
            )
            @RequestBody User user) {
        user.setId(id);
        return ResponseEntity.ok(userService.saveUser(user));
    }

    @Operation(
            summary = "Benutzer löschen",
            description = "Löscht einen Benutzer anhand seiner ID. Erlaubt für Administratoren oder den Benutzer selbst."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Benutzer erfolgreich gelöscht", content = @Content),
            @ApiResponse(responseCode = "403", description = "Zugriff verboten", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isSelf(#id)")
    @DeleteMapping("/{id}")
    public ResponseEntity<User> deleteUserByID(
            @Parameter(description = "ID des zu löschenden Benutzers", example = "1", required = true)
            @PathVariable Long id) {
        userService.deleteUserByID(id);
        return ResponseEntity.ok().build();
    }
}