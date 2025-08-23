package de.acosci.tasks.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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

    // Wichtig: @JsonIgnore darf hier nicht stehen, wenn Projekt-ID mitgegeben werden soll
    @ManyToOne
    @JoinColumn(name = "project_id")
    @OnDelete(action = OnDeleteAction.CASCADE) // https://stackoverflow.com/questions/7197181/jpa-unidirectional-many-to-one-and-cascading-delete
    private Project project;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TimeRecord> timeRecords = new ArrayList<>();

    // todo abhängig von Uses Cases @Transient
    // MVP Minimal viable Product
    @Transient
    private boolean active;
}
