package de.acosci.tasks.controller.rest;

import de.acosci.tasks.model.dto.TaskCalendarFeedLinkDTO;
import de.acosci.tasks.service.TaskCalendarFeedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Task Calendar Feed", description = "Read-only Kalender-Feed für externe Kalender-Clients")
public class TaskCalendarFeedRestController {

    private final TaskCalendarFeedService taskCalendarFeedService;

    @Operation(summary = "Aktuellen Kalender-Feed-Link des angemeldeten Benutzers abrufen")
    @GetMapping("/api/v1/calendar/feed-link")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TaskCalendarFeedLinkDTO> getFeedLink() {
        return ResponseEntity.ok(taskCalendarFeedService.getFeedLinkForCurrentUser());
    }

    @Operation(summary = "Kalender-Feed-Token neu generieren")
    @PostMapping("/api/v1/calendar/feed-link/regenerate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TaskCalendarFeedLinkDTO> regenerateFeedLink() {
        return ResponseEntity.ok(taskCalendarFeedService.regenerateFeedLinkForCurrentUser());
    }

    @Operation(summary = "Öffentlichen Read-only ICS-Feed per Token abrufen")
    @GetMapping(value = "/api/v1/calendar/feed/{token}/{slug}.ics", produces = "text/calendar")
    public ResponseEntity<String> getCalendarFeed(@PathVariable String token, @PathVariable String slug) {
        try {
            String content = taskCalendarFeedService.renderCalendarFeedByToken(token);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/calendar;charset=UTF-8"));
            headers.setContentDisposition(ContentDisposition.inline().filename("tasks.ics").build());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(content);
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }
}