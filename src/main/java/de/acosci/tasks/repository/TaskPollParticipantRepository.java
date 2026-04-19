package de.acosci.tasks.repository;

import de.acosci.tasks.model.entity.TaskPollParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskPollParticipantRepository extends JpaRepository<TaskPollParticipant, Long> {
    List<TaskPollParticipant> findAllByPoll_Id(Long pollId);
    Optional<TaskPollParticipant> findByInvitationTokenHash(String invitationTokenHash);
    Optional<TaskPollParticipant> findByPoll_IdAndUser_Id(Long pollId, Long userId);
}
