package de.acosci.tasks.repository;

import de.acosci.tasks.model.entity.TaskRecurrenceRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRecurrenceRuleRepository extends JpaRepository<TaskRecurrenceRule, Long> {
    List<TaskRecurrenceRule> findAllByActiveTrue();
    List<TaskRecurrenceRule> findAllByTask_Id(Long taskId);
}
