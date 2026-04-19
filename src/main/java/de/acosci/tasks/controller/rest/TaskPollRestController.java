package de.acosci.tasks.controller.rest;

import de.acosci.tasks.model.dto.TaskPollFinalizeDTO;
import de.acosci.tasks.model.dto.TaskPollMailRequestDTO;
import de.acosci.tasks.model.dto.TaskPollOwnerSummaryDTO;
import de.acosci.tasks.model.dto.TaskPollResponseDTO;
import de.acosci.tasks.model.dto.TaskPollUpsertDTO;
import de.acosci.tasks.service.TaskPollMailService;
import de.acosci.tasks.service.TaskPollService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TaskPollRestController {

    private final TaskPollService taskPollService;
    private final TaskPollMailService taskPollMailService;

    @GetMapping("/api/v1/task-polls/owned")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TaskPollOwnerSummaryDTO>> getOwnedPolls() {
        return ResponseEntity.ok(taskPollService.getOwnedPolls());
    }

    @GetMapping("/api/v1/tasks/{taskId}/poll")
    @PreAuthorize("hasRole('ADMIN') or @taskSecurity.canViewTaskByEmail(#taskId, authentication.name)")
    public ResponseEntity<TaskPollResponseDTO> getPoll(@PathVariable Long taskId) {
        return ResponseEntity.ok(taskPollService.getPollByTask(taskId));
    }

    @PutMapping("/api/v1/tasks/{taskId}/poll")
    @PreAuthorize("hasRole('ADMIN') or @taskSecurity.canEditTaskByEmail(#taskId, authentication.name)")
    public ResponseEntity<TaskPollResponseDTO> savePoll(@PathVariable Long taskId, @RequestBody TaskPollUpsertDTO dto) {
        return ResponseEntity.status(HttpStatus.OK).body(taskPollService.savePoll(taskId, dto));
    }

    @DeleteMapping("/api/v1/tasks/{taskId}/poll")
    @PreAuthorize("hasRole('ADMIN') or @taskSecurity.canEditTaskByEmail(#taskId, authentication.name)")
    public ResponseEntity<Void> deletePoll(@PathVariable Long taskId) {
        taskPollService.deletePoll(taskId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/v1/tasks/{taskId}/poll/finalize")
    @PreAuthorize("hasRole('ADMIN') or @taskSecurity.canEditTaskByEmail(#taskId, authentication.name)")
    public ResponseEntity<TaskPollResponseDTO> finalizePoll(@PathVariable Long taskId, @RequestBody TaskPollFinalizeDTO dto) {
        return ResponseEntity.ok(taskPollService.finalizePoll(taskId, dto));
    }

    @PostMapping("/api/v1/tasks/{taskId}/poll/finalization-notification/send")
    @PreAuthorize("hasRole('ADMIN') or @taskSecurity.canEditTaskByEmail(#taskId, authentication.name)")
    public ResponseEntity<Void> sendFinalizationNotification(@PathVariable Long taskId, @RequestBody(required = false) TaskPollMailRequestDTO dto) {
        taskPollMailService.sendFinalizationNotification(taskId, dto);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/v1/tasks/{taskId}/poll/invitations/send")
    @PreAuthorize("hasRole('ADMIN') or @taskSecurity.canEditTaskByEmail(#taskId, authentication.name)")
    public ResponseEntity<Void> sendInvitations(@PathVariable Long taskId, @RequestBody(required = false) TaskPollMailRequestDTO dto) {
        taskPollMailService.sendInvitations(taskId, dto);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/v1/tasks/{taskId}/poll/reminders/send")
    @PreAuthorize("hasRole('ADMIN') or @taskSecurity.canEditTaskByEmail(#taskId, authentication.name)")
    public ResponseEntity<Void> sendReminders(@PathVariable Long taskId, @RequestBody(required = false) TaskPollMailRequestDTO dto) {
        taskPollMailService.sendReminders(taskId, dto);
        return ResponseEntity.noContent().build();
    }
}
