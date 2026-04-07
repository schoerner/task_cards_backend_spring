package de.acosci.tasks.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

/**
 * Dependency relation between two tasks within the same project.
 */
@Entity
@Table(name = "task_dependencies",
       uniqueConstraints = @UniqueConstraint(name = "uk_task_dependency", columnNames = {"blocking_task_id", "blocked_task_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskDependency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "blocking_task_id", nullable = false)
    private Task blockingTask;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "blocked_task_id", nullable = false)
    private Task blockedTask;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskDependency that = (TaskDependency) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
