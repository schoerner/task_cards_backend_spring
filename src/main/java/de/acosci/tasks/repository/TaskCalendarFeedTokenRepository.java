package de.acosci.tasks.repository;

import de.acosci.tasks.model.entity.TaskCalendarFeedToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskCalendarFeedTokenRepository extends JpaRepository<TaskCalendarFeedToken, Long> {
    Optional<TaskCalendarFeedToken> findByUser_Id(Long userId);
    Optional<TaskCalendarFeedToken> findByToken(String token);
}