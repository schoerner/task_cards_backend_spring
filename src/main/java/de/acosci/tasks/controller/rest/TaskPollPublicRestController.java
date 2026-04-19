package de.acosci.tasks.controller.rest;

import de.acosci.tasks.model.dto.TaskPollPublicResponseDTO;
import de.acosci.tasks.model.dto.TaskPollPublicSubmissionDTO;
import de.acosci.tasks.service.TaskPollService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class TaskPollPublicRestController {

    private final TaskPollService taskPollService;

    @GetMapping("/api/public/v1/task-polls/respond/{token}")
    public ResponseEntity<TaskPollPublicResponseDTO> getPublicPoll(@PathVariable String token) {
        return ResponseEntity.ok(taskPollService.getPublicPoll(token));
    }

    @PutMapping("/api/public/v1/task-polls/respond/{token}")
    public ResponseEntity<TaskPollPublicResponseDTO> submitPublicPoll(@PathVariable String token,
                                                                      @RequestBody TaskPollPublicSubmissionDTO dto) {
        return ResponseEntity.ok(taskPollService.submitPublicResponse(token, dto));
    }
}
