package de.acosci.tasks.service;

import de.acosci.tasks.model.entity.Task;
import de.acosci.tasks.model.entity.TimeRecord;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Map;

/*

 */
public interface TaskService {
    List<Task> getAllTasks();
    List<Task> getMyTasks();
    List<Task> getMyOwnedTasks();
    List<Task> getActiveTasks();
    Task saveTask(Task task) throws Exception;
    Task getTaskByID(Long id);
    Task updateTask(Task task);
    Task patchTask(Long id, Map<String, Object> updates);
    void deleteTaskByID(Long id);
    void deleteTask(Task task);
    TimeRecord getActiveTimeRecord(Long taskID);
    TimeRecord getActiveTimeRecord(Task task);
    Boolean isActive(Long taskID);
    Boolean isActive(Task task);
    Task startTask(Long taskId);
    Task stopTask(Long taskId);

}
