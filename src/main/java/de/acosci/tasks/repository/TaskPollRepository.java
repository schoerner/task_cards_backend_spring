package de.acosci.tasks.repository;

import de.acosci.tasks.model.entity.TaskPoll;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskPollRepository extends JpaRepository<TaskPoll, Long> {
    Optional<TaskPoll> findByTask_Id(Long taskId);
}
