package de.acosci.tasks.service.impl;

import de.acosci.tasks.model.entity.Task;
import de.acosci.tasks.model.entity.TimeRecord;
import de.acosci.tasks.repository.TaskRepository;
import de.acosci.tasks.repository.TimeRecordRepository;
import de.acosci.tasks.service.TaskService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl implements TaskService {

    @Autowired // inject repository
    private TaskRepository taskRepository;

    @Autowired
    private TimeRecordRepository timeRecordRepository;

    @Override
    public List<Task> getAllTasks() {
        List<Task> allTasks = taskRepository.findAll();

        // update temporary active status for dto?
        allTasks.forEach(task -> {
            boolean active = task.getTimeRecords().stream()
                    .anyMatch(timeRecord -> timeRecord.getTimeEnd() == null);
            task.setActive(active);
        });

        return allTasks;
    }

    @Override
    public List<Task> getActiveTasks() {
        List<Task> allTasks = getAllTasks();
        return allTasks.stream().filter(Task::isActive).collect(Collectors.toList());
    }

    @Override
    public TimeRecord getActiveTimeRecord(Long taskID) throws EntityNotFoundException {
        Task task = taskRepository.findById(taskID)
                .orElseThrow(() -> new EntityNotFoundException("The task with id " + taskID + " was not found."));

        return getActiveTimeRecord(task);
    }

    @Override
    public TimeRecord getActiveTimeRecord(Task task) throws EntityNotFoundException {
        List<TimeRecord> timeRecords = task.getTimeRecords();

        return timeRecords.stream()
                .filter( timeRecord -> timeRecord.getTimeEnd() == null)
                .findFirst().orElse(null);
    }

    @Override
    public Boolean isActive(Long taskID) {
        Task task = taskRepository.findById(taskID)
                .orElseThrow(() -> new EntityNotFoundException("The task with id " + taskID + " was not found."));
        TimeRecord activeTimeRecord = getActiveTimeRecord(task);
        return activeTimeRecord != null;
    }

    @Override
    public Boolean isActive(Task task) {
        TimeRecord activeTimeRecord = getActiveTimeRecord(task);
        return activeTimeRecord != null;
    }

    @Override
    public Task startTask(Long taskID) throws EntityNotFoundException {
        Task task = taskRepository.findById(taskID)
                .orElseThrow(() -> new EntityNotFoundException("The task with id " + taskID + " was not found."));
        return startTask(task);
    }

    @Override
    public Task stopTask(Long taskID) throws EntityNotFoundException {
        Task task = taskRepository.findById(taskID)
                .orElseThrow(() -> new EntityNotFoundException("The task with id " + taskID + " was not found."));
        return stopTask(task);
    }

    @Override
    public Task startTask(Task task) {
        TimeRecord newTimeRecord = new TimeRecord(task);
        task.getTimeRecords().add(newTimeRecord);

        var savedTimeRecord = timeRecordRepository.save(newTimeRecord);
        var startedTask = taskRepository.save(task);

        startedTask.setActive(true);

        return startedTask;
    }

    @Override
    public Task stopTask(Task task) {
        var activeTimeRecord = getActiveTimeRecord(task);

        activeTimeRecord.setTimeEnd( new Date() );

        task.setActive(false);

        timeRecordRepository.save(activeTimeRecord);

        return task;
    }

    @Override public List<Task> getTasksByUserID(Long userID) {
        return null;//taskRepository.findByCreatorID(userID);
    }

    @Override
    public Task saveTask(Task task) throws Exception {
        return taskRepository.save(task);
    }

    @Override
    public Task getTaskByID(Long id) throws EntityNotFoundException {
        return taskRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("The task with id " + id + " was not found."));
    }

    @Override
    public void deleteTaskByID(Long id) {
        taskRepository.deleteById(id);
    }

    @Override
    public void deleteTask(Task task) {
        taskRepository.delete(task);
    }
}
