package de.acosci.tasks.service;

import de.acosci.tasks.model.entity.Task;

import java.util.List;

public interface ITaskService {
    List<Task> getTasks();
    List<Task> getTasksByUserID(Long userID);
    Task saveTask(Task task);
    Task getTaskByID(Long id);
    void deleteTaskByID(Long id);
    void deleteTask(Task task);
}
