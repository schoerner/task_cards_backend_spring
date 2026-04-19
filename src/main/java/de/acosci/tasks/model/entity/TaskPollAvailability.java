package de.acosci.tasks.model.entity;

import de.acosci.tasks.model.enums.TaskPollAvailabilityStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "task_poll_availabilities",
        uniqueConstraints = @UniqueConstraint(name = "uk_poll_participant_slot", columnNames = {"participant_id", "slot_start_at"}))
@Getter
@Setter
@NoArgsConstructor
public class TaskPollAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participant_id", nullable = false)
    private TaskPollParticipant participant;

    @Column(name = "slot_start_at", nullable = false)
    private OffsetDateTime slotStartAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskPollAvailabilityStatus availability;
}
