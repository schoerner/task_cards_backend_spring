package de.acosci.tasks.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Transient
    private Boolean active;

    @Transient
    private TimeRecord activeTimeRecord;

    @ManyToOne
    @JoinColumn(name = "creator_user_id")
    private User creator;

    @OneToMany(mappedBy = "task")
    private List<TimeRecord> timeRecords;

    public Boolean getActive() {
        active = isActive();
        return active;
    }

    public TimeRecord getActiveTimeRecord() {
        activeTimeRecord = timeRecords.stream().filter(timeRecord -> timeRecord.isActive()).findFirst().orElse(null);
        return activeTimeRecord;
    }

    public Boolean isActive() {
        return timeRecords.stream().anyMatch( timeRecord -> timeRecord.isActive());
    }
}
