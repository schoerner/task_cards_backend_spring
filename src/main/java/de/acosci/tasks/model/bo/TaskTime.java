package de.acosci.tasks.model.bo;

import de.acosci.tasks.model.entity.Task;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class TaskTime {
    private Task task;
    long milliseconds;
    long seconds;
    long minutes;
    long hours;
    long days;

    public long getMilliseconds() {
        AtomicLong milliseconds = new AtomicLong(0L);
        task.getTimeRecords().stream().forEach(timeRecord -> {
            milliseconds.addAndGet(timeRecord.getTimeEnd().getTime() - timeRecord.getTimeStart().getTime());
        });
        return milliseconds.get();
    }

    public long getSeconds() {
        seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds);
        return seconds;
    }

    public long getMinutes() {
        minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds);
        return minutes;
    }

    public long getHours() {
        hours = TimeUnit.MILLISECONDS.toHours(milliseconds);
        return hours;
    }

    public long getDays() {
        days = TimeUnit.MILLISECONDS.toDays(milliseconds);
        return days;
    }
}
