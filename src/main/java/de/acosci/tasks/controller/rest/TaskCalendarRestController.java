package de.acosci.tasks.controller.rest;

import de.acosci.tasks.model.dto.TaskCalendarEntryDTO;
import de.acosci.tasks.service.TaskCalendarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Task Calendar", description = "REST-API für Kalenderansichten von Tasks")
public class TaskCalendarRestController {

    private final TaskCalendarService taskCalendarService;

    @Operation(summary = "Terminierte Tasks des aktuell angemeldeten Benutzers für den Kalender anzeigen")
    @GetMapping("/api/v1/calendar/tasks")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TaskCalendarEntryDTO>> getMyCalendarTasks() {
        return ResponseEntity.ok(taskCalendarService.getCalendarTasksForCurrentUser());
    }
}