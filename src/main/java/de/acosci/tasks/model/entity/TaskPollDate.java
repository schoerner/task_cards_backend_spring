package de.acosci.tasks.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "task_poll_dates")
@Getter
@Setter
@NoArgsConstructor
public class TaskPollDate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "poll_id", nullable = false)
    private TaskPoll poll;

    @Column(name = "poll_date", nullable = false)
    private LocalDate pollDate;
}
