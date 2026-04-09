package de.acosci.tasks.service.impl;

import de.acosci.tasks.model.dto.TaskCalendarEntryDTO;
import de.acosci.tasks.model.dto.TaskCalendarFeedLinkDTO;
import de.acosci.tasks.model.entity.TaskCalendarFeedToken;
import de.acosci.tasks.model.entity.User;
import de.acosci.tasks.repository.TaskCalendarFeedTokenRepository;
import de.acosci.tasks.repository.UserRepository;
import de.acosci.tasks.service.TaskCalendarFeedService;
import de.acosci.tasks.service.TaskCalendarService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskCalendarFeedServiceImpl implements TaskCalendarFeedService {

    private final TaskCalendarFeedTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final TaskCalendarService taskCalendarService;
    private final TaskCalendarIcsRenderer taskCalendarIcsRenderer;

    @Value("${app.calendar.feed-base-url:http://localhost:8080}")
    private String calendarFeedBaseUrl;

    @Override
    @Transactional(readOnly = true)
    public TaskCalendarFeedLinkDTO getFeedLinkForCurrentUser() {
        User user = getCurrentUser();

        return tokenRepository.findByUser_Id(user.getId())
                .map(this::toDto)
                .orElseGet(() -> {
                    TaskCalendarFeedLinkDTO dto = new TaskCalendarFeedLinkDTO();
                    dto.setTokenGenerated(false);
                    dto.setFeedUrl(null);
                    dto.setTokenHint(null);
                    return dto;
                });
    }

    @Override
    public TaskCalendarFeedLinkDTO regenerateFeedLinkForCurrentUser() {
        User user = getCurrentUser();
        OffsetDateTime now = OffsetDateTime.now();

        TaskCalendarFeedToken entity = tokenRepository.findByUser_Id(user.getId())
                .orElseGet(TaskCalendarFeedToken::new);

        entity.setUser(user);
        entity.setToken(generateSecureToken());
        entity.setUpdatedAt(now);

        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(now);
        }

        TaskCalendarFeedToken saved = tokenRepository.save(entity);
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public String renderCalendarFeedByToken(String token) {
        TaskCalendarFeedToken feedToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new EntityNotFoundException("Calendar feed token not found"));

        Long userId = feedToken.getUser().getId();
        List<TaskCalendarEntryDTO> tasks = taskCalendarService.getCalendarTasksForUser(userId);

        return taskCalendarIcsRenderer.render(tasks);
    }

    private TaskCalendarFeedLinkDTO toDto(TaskCalendarFeedToken entity) {
        TaskCalendarFeedLinkDTO dto = new TaskCalendarFeedLinkDTO();
        dto.setTokenGenerated(true);
        dto.setFeedUrl(buildFeedUrl(entity.getToken()));
        dto.setTokenHint(entity.getToken().substring(0, Math.min(8, entity.getToken().length())) + "...");
        return dto;
    }

    private String buildFeedUrl(String token) {
        return calendarFeedBaseUrl + "/api/v1/calendar/feed/" + token + "/ACoSci-Tasks.ics";
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + email));
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}