package de.acosci.tasks.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.Date;

@Entity
@Table(name = "time_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // https://www.baeldung.com/jackson-jsonformat
    //@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SS")
    @Column(name = "time_start")
    private Date timeStart;

    //@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SS")
    @Column(name = "time_end")
    private Date timeEnd;

    /* Lazy Loading is a design pattern that we use to defer the initialization of an object as long as it’s possible. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    @JsonIgnore
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Task task;

    public TimeRecord(Task task) {
        this.task = task;
        timeStart = new Date();
        timeEnd = null;
    }

}
