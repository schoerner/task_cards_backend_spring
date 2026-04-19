package de.acosci.tasks.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "task_poll_participants")
@Getter
@Setter
@NoArgsConstructor
public class TaskPollParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "poll_id", nullable = false)
    private TaskPoll poll;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "external_email", length = 255)
    private String externalEmail;

    @Column(name = "display_name", length = 255)
    private String displayName;

    @Column(name = "response_name", length = 255)
    private String responseName;

    @Column(name = "invitation_token_hash", length = 255)
    private String invitationTokenHash;

    @Column(name = "invitation_token", length = 120)
    private String invitationToken;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    private OffsetDateTime invitedAt;
    private OffsetDateTime respondedAt;
    private OffsetDateTime lastReminderAt;

    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TaskPollAvailability> availabilities = new LinkedHashSet<>();
}
