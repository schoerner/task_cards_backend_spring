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
    private int numberOfTasks;
    private Long meanTimePerTask;
    private Long totalTime;
    private Long maxTimeOfTasks;

    public int getNumberOfTasks() {
        numberOfTasks = tasks.size();
        return numberOfTasks;
    }

    public Long getMeanTimePerTask() {
        AtomicLong sumMinutes = new AtomicLong(0L);
        tasks.stream().forEach(task -> {
            task.getTimeRecords().stream().forEach(timeRecord -> {
                long diffInMillies = timeRecord.getTimeEnd().getTime() - timeRecord.getTimeStart().getTime();
                sumMinutes.addAndGet(TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS));
            });
        });
        meanTimePerTask = sumMinutes.get() / tasks.size();
        return meanTimePerTask;
    }

    public Long getMaxTime() {
        AtomicLong maxTime = new AtomicLong(0L);
        tasks.stream().forEach(task -> {
            task.getTimeRecords().stream().forEach(timeRecord -> {
                long diffInMillies = timeRecord.getTimeEnd().getTime() - timeRecord.getTimeStart().getTime();
                if(diffInMillies > maxTime.get()) {
                    maxTime.set(diffInMillies);
                }
            });
        });
        return maxTime.get();
    }


}
