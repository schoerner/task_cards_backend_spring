package de.acosci.tasks.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

/**
 * Checklist-like subtask belonging to one task.
 */
@Entity
@Table(name = "task_subtasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskSubTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false)
    private boolean completed = false;

    @Column(nullable = false)
    private Integer position = 0;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskSubTask that = (TaskSubTask) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
