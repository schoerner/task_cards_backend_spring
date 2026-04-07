package de.acosci.tasks.controller.rest;

import de.acosci.tasks.model.dto.TaskCommentCreateDTO;
import de.acosci.tasks.model.dto.TaskCommentResponseDTO;
import de.acosci.tasks.model.entity.TaskComment;
import de.acosci.tasks.model.mapper.TaskCommentMapper;
import de.acosci.tasks.service.TaskCommentService;
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
@RequestMapping("/api/v1/tasks/{taskId}/comments")
@RequiredArgsConstructor
@Tag(name = "Task Comments", description = "REST-API zur Verwaltung von Task-Kommentaren")
public class TaskCommentRestController {

    private final TaskCommentService taskCommentService;

    @Operation(summary = "Kommentare einer Task anzeigen")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or @taskSecurity.canViewTaskByEmail(#taskId, authentication.name)")
    public ResponseEntity<List<TaskCommentResponseDTO>> getComments(@PathVariable Long taskId) {
        List<TaskCommentResponseDTO> result = taskCommentService.getCommentsByTask(taskId)
                .stream()
                .map(TaskCommentMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Kommentar zu einer Task anlegen")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or @taskSecurity.canEditTaskByEmail(#taskId, authentication.name)")
    public ResponseEntity<TaskCommentResponseDTO> createComment(@PathVariable Long taskId,
                                                                @Valid @RequestBody TaskCommentCreateDTO dto) {
        TaskComment comment = taskCommentService.createComment(taskId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(TaskCommentMapper.toResponseDTO(comment));
    }

    @Operation(summary = "Kommentar löschen")
    @DeleteMapping("/{commentId}")
    @PreAuthorize("hasRole('ADMIN') or @taskSecurity.canEditTaskByEmail(#taskId, authentication.name)")
    public ResponseEntity<Void> deleteComment(@PathVariable Long taskId, @PathVariable Long commentId) {
        taskCommentService.deleteComment(taskId, commentId);
        return ResponseEntity.noContent().build();
    }
}
