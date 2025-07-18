package de.acosci.tasks.model.bo;

import de.acosci.tasks.model.entity.Task;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskStatistics {
    private List<Task> tasks;
    private Long numberOfTasks;
    private Long meanTimePerTask;

    public int getNumberOfTasks() {
        return tasks.size();
    }

    public Long getMeanTimePerTask() {
        AtomicLong sumMinutes = new AtomicLong(0L);
        tasks.stream().forEach(task -> {
            task.getTimeRecords().stream().forEach(timeRecord -> {
                long diffInMillies = timeRecord.getTimeEnd().getTime() - timeRecord.getTimeStart().getTime();
                sumMinutes.addAndGet(TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS));
            });
        });
        Long meanMinutes = sumMinutes.get() / tasks.size();
        return meanMinutes;
    }
}
