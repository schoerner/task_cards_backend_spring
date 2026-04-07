package de.acosci.tasks.model.entity;

import de.acosci.tasks.model.enums.TaskRecurrenceFrequency;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Recurrence rule assigned to a task. The execution logic can be added later.
 */
@Entity
@Table(name = "task_recurrence_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskRecurrenceRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskRecurrenceFrequency frequency;

    @Column(nullable = false)
    private Integer intervalValue = 1;

    private OffsetDateTime nextExecutionAt;

    @Column(nullable = false)
    private boolean active = true;

    @Column(length = 500)
    private String customCronExpression;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskRecurrenceRule that = (TaskRecurrenceRule) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
