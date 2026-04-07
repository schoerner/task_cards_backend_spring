package de.acosci.tasks.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.acosci.tasks.model.enums.TaskPriority;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Work item on the kanban board of a project.
 */
@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "board_column_id", nullable = false)
    private BoardColumn boardColumn;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creator_user_id", nullable = false)
    private User creator;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskPriority priority = TaskPriority.MEDIUM;

    private OffsetDateTime dueDate;

    @Column(nullable = false)
    private boolean archived = false;

    @Column(nullable = false)
    private Integer estimatedMinutes = 0;

    /**
     * Cached total of completed time records in minutes.
     * This value is recalculated whenever active tracking is stopped.
     */
    @Column(nullable = false)
    private Integer trackedMinutes = 0;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    @ManyToMany
    @JoinTable(
            name = "task_assignees",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> assignees = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(
            name = "task_labels_map",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "label_id")
    )
    private Set<TaskLabel> labels = new LinkedHashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TaskComment> comments = new LinkedHashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TaskSubTask> subTasks = new LinkedHashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TaskAttachment> attachments = new LinkedHashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TaskActivity> activities = new LinkedHashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TaskRecurrenceRule> recurrenceRules = new LinkedHashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "blockedTask", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TaskDependency> predecessors = new LinkedHashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "blockingTask", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TaskDependency> successors = new LinkedHashSet<>();

    /**
     * Recorded working intervals for active/manual time tracking.
     */
    @JsonIgnore
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TimeRecord> timeRecords = new LinkedHashSet<>();

    /**
     * Convenience flag for the frontend. A task is active when one open
     * time record without end timestamp exists.
     */
    @Transient
    public boolean isActive() {
        return timeRecords != null
                && timeRecords.stream().anyMatch(timeRecord -> timeRecord.getTimeEnd() == null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id != null && Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}