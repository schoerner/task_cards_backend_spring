package de.acosci.tasks.controller.rest;

import de.acosci.tasks.model.entity.Project;
import de.acosci.tasks.model.entity.User;
import de.acosci.tasks.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/projects")
//@PreAuthorize("hasRole('MODERATOR')")  // Optional, filtered via FilterChain
@RequiredArgsConstructor
@Tag(name = "Projects", description = "REST-API zur Verwaltung von Projekten")
public class ProjectRestController {
    private final ProjectService projectService;

    /** Member can access the projects they are participating on. */
    @Operation(
            summary = "Eigene Projekte als Mitglied abrufen",
            description = "Liefert alle Projekte, bei denen der aktuell angemeldete Benutzer Mitglied ist."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Projekte erfolgreich geladen",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Project.class)))
            ),
            @ApiResponse(responseCode = "403", description = "Zugriff verboten", content = @Content)
    })
    @GetMapping("/my")
    public ResponseEntity<List<Project>> getMyProjects() {
        User current = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(projectService.getAllProjectsByMembersID(current.getId()));
    }

    /** Creators ar owners of projects, they get their projects here. */
    @Operation(
            summary = "Eigene Projekte als Besitzer abrufen",
            description = "Liefert alle Projekte, die der aktuell angemeldete Benutzer erstellt hat."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Eigene Projekte erfolgreich geladen",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Project.class)))
            ),
            @ApiResponse(responseCode = "403", description = "Zugriff verboten", content = @Content)
    })
    @GetMapping("/my-owned")
    public ResponseEntity<List<Project>> getMyOwnedProjects() {
        User current = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(projectService.getAllProjectsByCreatorID(current.getId()));
    }

    @Operation(
            summary = "Projekt per ID abrufen",
            description = "Liefert ein einzelnes Projekt anhand seiner ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Projekt erfolgreich gefunden",
                    content = @Content(schema = @Schema(implementation = Project.class))
            ),
            @ApiResponse(responseCode = "404", description = "Projekt nicht gefunden", content = @Content),
            @ApiResponse(responseCode = "403", description = "Zugriff verboten", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<Project> getProject(
            @Parameter(description = "ID des Projekts", example = "1", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(projectService.findById(id));
    }

    /** Admins and moderators are able to create projects. */
    @Operation(
            summary = "Projekt anlegen",
            description = "Legt ein neues Projekt an. Der Ersteller wird im Service aus dem SecurityContext gesetzt."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Projekt erfolgreich angelegt",
                    content = @Content(schema = @Schema(implementation = Project.class))
            ),
            @ApiResponse(responseCode = "400", description = "Ungültige Projektdaten", content = @Content),
            @ApiResponse(responseCode = "403", description = "Zugriff verboten", content = @Content)
    })
    @PostMapping
    public ResponseEntity<Project> createProject(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Das anzulegende Projekt",
                    required = true,
                    content = @Content(schema = @Schema(implementation = Project.class))
            )
            @RequestBody Project project) {
        return ResponseEntity.ok(projectService.save(project));
    }

    /** Update projects. */
    @Operation(
            summary = "Projekt aktualisieren",
            description = "Aktualisiert ein bestehendes Projekt anhand seiner ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Projekt erfolgreich aktualisiert",
                    content = @Content(schema = @Schema(implementation = Project.class))
            ),
            @ApiResponse(responseCode = "400", description = "Ungültige Projektdaten", content = @Content),
            @ApiResponse(responseCode = "403", description = "Zugriff verboten", content = @Content),
            @ApiResponse(responseCode = "404", description = "Projekt nicht gefunden", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<Project> updateProject(
            @Parameter(description = "ID des zu aktualisierenden Projekts", example = "1", required = true)
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Die neuen Projektdaten",
                    required = true,
                    content = @Content(schema = @Schema(implementation = Project.class))
            )
            @RequestBody Project project) {
        project.setId(id);
        return ResponseEntity.ok(projectService.save(project));
    }

    /** Delete projects by owners or admins. */
    @Operation(
            summary = "Projekt löschen",
            description = "Löscht ein Projekt anhand seiner ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Projekt erfolgreich gelöscht", content = @Content),
            @ApiResponse(responseCode = "403", description = "Zugriff verboten", content = @Content),
            @ApiResponse(responseCode = "404", description = "Projekt nicht gefunden", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(
            @Parameter(description = "ID des zu löschenden Projekts", example = "1", required = true)
            @PathVariable Long id) {
        projectService.deleteById(id);
        return ResponseEntity.ok().build();
    }
}