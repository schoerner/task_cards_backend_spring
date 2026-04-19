package de.acosci.tasks.model.entity;

import de.acosci.tasks.model.enums.TaskPollStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "task_polls")
@Getter
@Setter
@NoArgsConstructor
public class TaskPoll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false, unique = true)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String description;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private LocalTime dayStartTime;

    @Column(nullable = false)
    private LocalTime dayEndTime;

    @Column(nullable = false)
    private Integer slotMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskPollStatus status = TaskPollStatus.OPEN;

    private OffsetDateTime finalizedStartAt;
    private OffsetDateTime finalizedEndAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "poll", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TaskPollDate> dates = new LinkedHashSet<>();

    @OneToMany(mappedBy = "poll", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TaskPollParticipant> participants = new LinkedHashSet<>();
}
