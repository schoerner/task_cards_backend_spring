package de.acosci.tasks.controller.rest;

import de.acosci.tasks.model.dto.TaskResponseDTO;
import de.acosci.tasks.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Task Focus", description = "REST-API für die Focus-Page")
public class TaskFocusRestController {

    private final TaskService taskService;

    @Operation(summary = "Focus-Tasks des aktuellen Users abrufen")
    @GetMapping("/api/v1/tasks/focus")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TaskResponseDTO>> getFocusTasks(
            @RequestParam(defaultValue = "10") Integer limit
    ) {
        return ResponseEntity.ok(taskService.getFocusTasks(limit));
    }
}