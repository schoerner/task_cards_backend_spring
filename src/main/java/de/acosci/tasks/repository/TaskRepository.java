package de.acosci.tasks.repository;

import de.acosci.tasks.model.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllByProject_IdAndArchivedFalse(Long projectId);
    List<Task> findAllByProject_IdAndBoardColumn_IdAndArchivedFalse(Long projectId, Long boardColumnId);
    List<Task> findAllByAssignees_IdAndArchivedFalse(Long assigneeUserId);

    @Query("""
        select distinct t
        from Task t
        join fetch t.project p
        join fetch t.boardColumn bc
        left join fetch t.timeRecords tr
        left join fetch t.calendarReminders cr
        left join fetch t.assignees a
        left join fetch a.profile ap
        where t.dueDate is not null
          and (t.creator.id = :userId or a.id = :userId)
        order by t.dueDate asc, t.id asc
        """)
    List<Task> findCalendarTasksForUser(Long userId);
}
