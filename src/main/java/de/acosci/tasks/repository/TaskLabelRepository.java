package de.acosci.tasks.repository;

import de.acosci.tasks.model.entity.TaskLabel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskLabelRepository extends JpaRepository<TaskLabel, Long> {
    List<TaskLabel> findAllByProject_IdOrderByNameAsc(Long projectId);
    Optional<TaskLabel> findByProject_IdAndName(Long projectId, String name);
}
