package de.acosci.tasks.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "user_favorite_tasks")
@Getter
@Setter
@NoArgsConstructor
public class UserFavoriteTask {

    @EmbeddedId
    private UserFavoriteTaskId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("taskId")
    @JoinColumn(name = "task_id", nullable = false)
    @JsonIgnore
    private Task task;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public UserFavoriteTask(User user, Task task) {
        this.id = new UserFavoriteTaskId(user.getId(), task.getId());
        this.user = user;
        this.task = task;
    }
}