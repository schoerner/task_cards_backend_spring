package de.acosci.tasks.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data // @Data: Combines @Getter, @Setter, @ToString, @EqualsAndHashCode, and @RequiredArgsConstructor
@Entity // JPA-Entity
@Table(name="tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    @ManyToOne
    @JoinColumn(name = "creator_user_id")
    private User creator;

    @OneToMany(mappedBy = "task")
    private List<TimeRecord> timeRecords = new ArrayList<>();

    // todo abhängig von Uses Cases @Transient
    // MVP Minimal viable Product
    @Transient
    private boolean active;
}
