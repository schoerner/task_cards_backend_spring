package de.acosci.tasks.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data // @Data: Combines @Getter, @Setter, @ToString, @EqualsAndHashCode, and @RequiredArgsConstructor
@Entity // JPA-Entity
@Table(name="tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @ManyToOne
    @JoinColumn(name = "creator_user_id")
    private User creator;

}
