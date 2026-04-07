package de.acosci.tasks.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Project-local label usable for filtering and visual grouping of tasks.
 */
@Entity
@Table(name = "task_labels", uniqueConstraints = @UniqueConstraint(name = "uk_project_label_name", columnNames = {"project_id", "name"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskLabel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 30)
    private String color;

    @JsonIgnore
    @ManyToMany(mappedBy = "labels")
    private Set<Task> tasks = new LinkedHashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskLabel taskLabel = (TaskLabel) o;
        return id != null && Objects.equals(id, taskLabel.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
