package de.acosci.tasks.repository;

import de.acosci.tasks.model.entity.TaskPollAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskPollAvailabilityRepository extends JpaRepository<TaskPollAvailability, Long> {
    List<TaskPollAvailability> findAllByParticipant_Poll_Id(Long pollId);
    List<TaskPollAvailability> findAllByParticipant_Id(Long participantId);
    void deleteByParticipant_Id(Long participantId);
}
