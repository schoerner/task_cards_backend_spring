package de.acosci.tasks.controller.rest;

import de.acosci.tasks.model.entity.Task;
import de.acosci.tasks.service.TaskService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost", "http://localhost:5173/", "http://localhost:3000/"})
@Tag(name = "Tasks", description = "REST-API zur Verwaltung von Tasks")
public class TaskRestController {

    private final TaskService taskService;

    @Operation(
            summary = "Alle sichtbaren Tasks abrufen",
            description = "Liefert alle Tasks des aktuellen Benutzers beziehungsweise alle erlaubten Tasks für Administratoren."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Tasks erfolgreich geladen",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Task.class)))
            ),
            @ApiResponse(responseCode = "403", description = "Zugriff verboten", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        try {
            return ResponseEntity.ok(taskService.getAllTasks());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @Operation(
            summary = "Eigene Tasks abrufen",
            description = "Liefert alle Tasks, an denen der aktuelle Benutzer beteiligt ist."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Eigene Tasks erfolgreich geladen",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Task.class)))
            ),
            @ApiResponse(responseCode = "404", description = "Keine Tasks gefunden", content = @Content)
    })
    @GetMapping("/my")
    public ResponseEntity<List<Task>> getMyTasks() {
        try {
            return new ResponseEntity<>(taskService.getMyTasks(), HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Eigene erstellte Tasks abrufen",
            description = "Liefert alle Tasks, die der aktuelle Benutzer selbst erstellt hat."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Eigene erstellte Tasks erfolgreich geladen",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Task.class)))
            ),
            @ApiResponse(responseCode = "404", description = "Keine Tasks gefunden", content = @Content)
    })
    @GetMapping("/my/owned")
    public ResponseEntity<List<Task>> getMyOwnedTasks() {
        try {
            return new ResponseEntity<>(taskService.getMyOwnedTasks(), HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Task per ID abrufen",
            description = "Liefert einen Task anhand seiner ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Task erfolgreich gefunden",
                    content = @Content(schema = @Schema(implementation = Task.class))
            ),
            @ApiResponse(responseCode = "404", description = "Task nicht gefunden", content = @Content),
            @ApiResponse(responseCode = "403", description = "Zugriff verboten", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(
            @Parameter(description = "ID des Tasks", example = "1", required = true)
            @PathVariable Long id) {
        try {
            return ResponseEntity.ok(taskService.getTaskByID(id));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @Operation(
            summary = "Task anlegen",
            description = "Legt einen neuen Task an."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Task erfolgreich angelegt",
                    content = @Content(schema = @Schema(implementation = Task.class))
            ),
            @ApiResponse(responseCode = "400", description = "Ungültige Task-Daten", content = @Content),
            @ApiResponse(responseCode = "403", description = "Zugriff verboten", content = @Content)
    })
    @PostMapping
    public ResponseEntity<Task> createTask(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Der anzulegende Task",
                    required = true,
                    content = @Content(schema = @Schema(implementation = Task.class))
            )
            @RequestBody Task task) {
        try {
            Task saved = taskService.saveTask(task);
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(
            summary = "Task vollständig aktualisieren",
            description = "Aktualisiert einen bestehenden Task vollständig. Die ID in URL und Request-Body muss übereinstimmen."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Task erfolgreich aktualisiert",
                    content = @Content(schema = @Schema(implementation = Task.class))
            ),
            @ApiResponse(responseCode = "400", description = "Ungültige oder inkonsistente Task-Daten", content = @Content),
            @ApiResponse(responseCode = "404", description = "Task nicht gefunden", content = @Content),
            @ApiResponse(responseCode = "403", description = "Zugriff verboten", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(
            @Parameter(description = "ID des zu aktualisierenden Tasks", example = "1", required = true)
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Die vollständigen neuen Task-Daten",
                    required = true,
                    content = @Content(schema = @Schema(implementation = Task.class))
            )
            @RequestBody Task task) {
        try {
            if (!id.equals(task.getId())) {
                return ResponseEntity.badRequest().build();
            }
            Task updated = taskService.updateTask(task);
            return ResponseEntity.ok(updated);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @Operation(
            summary = "Task teilweise aktualisieren",
            description = "Aktualisiert einzelne Felder eines Tasks per Patch."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Task erfolgreich aktualisiert",
                    content = @Content(schema = @Schema(implementation = Task.class))
            ),
            @ApiResponse(responseCode = "400", description = "Ungültige Patch-Daten", content = @Content),
            @ApiResponse(responseCode = "404", description = "Task nicht gefunden", content = @Content),
            @ApiResponse(responseCode = "403", description = "Zugriff verboten", content = @Content)
    })
    @PatchMapping("/{id}")
    public ResponseEntity<Task> patchTask(
            @Parameter(description = "ID des zu aktualisierenden Tasks", example = "1", required = true)
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Zu ändernde Felder als JSON-Objekt",
                    required = true
            )
            @RequestBody Map<String, Object> updates) {

        try {
            Task patched = taskService.patchTask(id, updates);
            return ResponseEntity.ok(patched);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @Operation(
            summary = "Task löschen",
            description = "Löscht einen Task anhand seiner ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Task erfolgreich gelöscht", content = @Content),
            @ApiResponse(responseCode = "404", description = "Task nicht gefunden", content = @Content),
            @ApiResponse(responseCode = "403", description = "Zugriff verboten", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @Parameter(description = "ID des zu löschenden Tasks", example = "1", required = true)
            @PathVariable Long id) {
        try {
            taskService.deleteTaskByID(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @Operation(
            summary = "Task starten",
            description = "Startet die Bearbeitung beziehungsweise Zeitmessung eines Tasks."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Task erfolgreich gestartet",
                    content = @Content(schema = @Schema(implementation = Task.class))
            ),
            @ApiResponse(responseCode = "404", description = "Task nicht gefunden", content = @Content),
            @ApiResponse(responseCode = "403", description = "Zugriff verboten", content = @Content)
    })
    @PostMapping("/{id}/start")
    public ResponseEntity<Task> startTask(
            @Parameter(description = "ID des zu startenden Tasks", example = "1", required = true)
            @PathVariable Long id) {
        try {
            return ResponseEntity.ok(taskService.startTask(id));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @Operation(
            summary = "Task stoppen",
            description = "Stoppt die Bearbeitung beziehungsweise Zeitmessung eines Tasks."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Task erfolgreich gestoppt",
                    content = @Content(schema = @Schema(implementation = Task.class))
            ),
            @ApiResponse(responseCode = "404", description = "Task nicht gefunden", content = @Content),
            @ApiResponse(responseCode = "403", description = "Zugriff verboten", content = @Content)
    })
    @PostMapping("/{id}/stop")
    public ResponseEntity<Task> stopTask(
            @Parameter(description = "ID des zu stoppenden Tasks", example = "1", required = true)
            @PathVariable Long id) {
        try {
            return ResponseEntity.ok(taskService.stopTask(id));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}