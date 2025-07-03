package de.acosci.tasks.service;

import de.acosci.tasks.model.entity.Task;
import de.acosci.tasks.repository.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService implements ITaskService {

    @Autowired // inject repository
    private TaskRepository taskRepository;

    @Override
    public List<Task> getTasks() {
        List<Task> tmp = taskRepository.findAll();
        tmp.stream().forEach(task -> { task.isActive(); });
        return tmp;
        //return taskRepository.findAll();
    }

    @Override public List<Task> getTasksByUserID(Long userID) {
        return null;//taskRepository.findByCreatorID(userID);
    }

    @Override
    public Task saveTask(Task task) {
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
