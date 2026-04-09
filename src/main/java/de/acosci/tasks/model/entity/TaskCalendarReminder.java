package de.acosci.tasks.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "task_calendar_reminders")
public class TaskCalendarReminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "task_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Task task;

    @Column(name = "minutes_before", nullable = false)
    private Integer minutesBefore;

    @Column(name = "action_type", nullable = false, length = 20)
    private String actionType = "DISPLAY";

    @Column(length = 1000)
    private String message;
}