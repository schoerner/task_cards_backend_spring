package de.acosci.tasks.controller.rest;

import de.acosci.tasks.model.dto.*;
import de.acosci.tasks.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "REST-API zur Verwaltung von Tasks")
public class TaskRestController {

    private final TaskService taskService;

    @Operation(summary = "Alle nicht archivierten Tasks eines Projekts anzeigen")
    @GetMapping("/api/v1/projects/{projectId}/tasks")
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.isMemberByEmail(#projectId, authentication.name)")
    public ResponseEntity<List<TaskResponseDTO>> getTasksByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(taskService.getTasksByProject(projectId));
    }

    @Operation(summary = "Task per ID abrufen")
    @GetMapping("/api/v1/tasks/{taskId}")
    @PreAuthorize("hasRole('ADMIN') or @taskSecurity.canViewTaskByEmail(#taskId, authentication.name)")
    public ResponseEntity<TaskResponseDTO> getTaskById(@PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.getTaskById(taskId));
    }

    @Operation(summary = "Task anlegen")
    @PostMapping("/api/v1/tasks")
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.canEditTasksByEmail(#dto.projectId, authentication.name)")
    public ResponseEntity<TaskResponseDTO> createTask(@Valid @RequestBody TaskCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(dto));
    }

    @Operation(summary = "Task aktualisieren")
    @PutMapping("/api/v1/tasks/{taskId}")
    @PreAuthorize("hasRole('ADMIN') or @taskSecurity.canEditTaskByEmail(#taskId, authentication.name)")
    public ResponseEntity<TaskResponseDTO> updateTask(@PathVariable Long taskId, @Valid @RequestBody TaskUpdateDTO dto) {
        return ResponseEntity.ok(taskService.updateTask(taskId, dto));
    }

    @PatchMapping("/api/v1/projects/{projectId}/board-columns/{boardColumnId}/task-order")
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.canEditTasksByEmail(#projectId, authentication.name)")
    public ResponseEntity<Void> reorderTasksInColumn(
            @PathVariable Long projectId,
            @PathVariable Long boardColumnId,
            @RequestBody TaskOrderUpdateDTO dto
    ) {
        taskService.reorderTasksInColumn(projectId, boardColumnId, dto.getTaskIds());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/api/v1/projects/{projectId}/task-order/move-between-columns")
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.canEditTasksByEmail(#projectId, authentication.name)")
    public ResponseEntity<Void> moveTaskBetweenColumns(
            @PathVariable Long projectId,
            @RequestBody TaskMoveBetweenColumnsDTO dto
    ) {
        taskService.moveTaskBetweenColumns(
                projectId,
                dto.getSourceColumnId(),
                dto.getTargetColumnId(),
                dto.getSourceTaskIds(),
                dto.getTargetTaskIds()
        );
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/api/v1/tasks/{taskId}/favorite")
    @PreAuthorize("hasRole('ADMIN') or @taskSecurity.canViewTaskByEmail(#taskId, authentication.name)")
    public ResponseEntity<TaskResponseDTO> setFavorite(
            @PathVariable Long taskId,
            @RequestParam boolean favorite
    ) {
        return ResponseEntity.ok(taskService.setFavorite(taskId, favorite));
    }

    @Operation(summary = "Task archivieren")
    @PatchMapping("/api/v1/tasks/{taskId}/archive")
    @PreAuthorize("hasRole('ADMIN') or @taskSecurity.canEditTaskByEmail(#taskId, authentication.name)")
    public ResponseEntity<TaskResponseDTO> archiveTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.archiveTask(taskId));
    }

    @Operation(summary = "Task wiederherstellen")
    @PatchMapping("/api/v1/tasks/{taskId}/restore")
    @PreAuthorize("hasRole('ADMIN') or @taskSecurity.canEditTaskByEmail(#taskId, authentication.name)")
    public ResponseEntity<TaskResponseDTO> restoreTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.restoreTask(taskId));
    }

    @Operation(summary = "Task löschen")
    @DeleteMapping("/api/v1/tasks/{taskId}")
    @PreAuthorize("hasRole('ADMIN') or @taskSecurity.canEditTaskByEmail(#taskId, authentication.name)")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Zeitbuchungen einer Task anzeigen")
    @GetMapping("/api/v1/tasks/{taskId}/time-records")
    @PreAuthorize("hasRole('ADMIN') or @taskSecurity.canViewTaskByEmail(#taskId, authentication.name)")
    public ResponseEntity<List<TimeRecordResponseDTO>> getTimeRecords(@PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.getTimeRecords(taskId));
    }

    @Operation(summary = "Prüfen, ob eine Task aktuell aktiv getrackt wird")
    @GetMapping("/api/v1/tasks/{taskId}/time-tracking/active")
    @PreAuthorize("hasRole('ADMIN') or @taskSecurity.canViewTaskByEmail(#taskId, authentication.name)")
    public ResponseEntity<Boolean> isActive(@PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.isActive(taskId));
    }

    @Operation(summary = "Aktives Zeittracking für eine Task starten")
    @PostMapping("/api/v1/tasks/{taskId}/time-tracking/start")
    @PreAuthorize("hasRole('ADMIN') or @taskSecurity.canEditTaskByEmail(#taskId, authentication.name)")
    public ResponseEntity<TaskResponseDTO> startTimeTracking(@PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.startTimeTracking(taskId));
    }

    @Operation(summary = "Aktives Zeittracking für eine Task stoppen")
    @PostMapping("/api/v1/tasks/{taskId}/time-tracking/stop")
    @PreAuthorize("hasRole('ADMIN') or @taskSecurity.canEditTaskByEmail(#taskId, authentication.name)")
    public ResponseEntity<TaskResponseDTO> stopTimeTracking(@PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.stopTimeTracking(taskId));
    }
}
