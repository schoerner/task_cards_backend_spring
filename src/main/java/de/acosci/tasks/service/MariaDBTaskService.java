package de.acosci.tasks.service;

import de.acosci.tasks.model.Task;
import de.acosci.tasks.repository.TaskNotFoundException;
import de.acosci.tasks.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MariaDBTaskService implements TaskService {

    @Autowired // inject repository
    private TaskRepository taskRepository;

    @Override
    public List<Task> getTasks() {
        return taskRepository.findAll();
    }

    @Override public List<Task> getTasksByUserID(Long userID) {
        return null;//taskRepository.findByCreatorID(userID);
    }

    @Override
    public Task saveTask(Task task) {
        return taskRepository.save(task);
    }

    @Override
    public Task getTaskByID(Long id) {
        return taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
        /*
        Optional<Task> optional = taskRepository.findById(id);
        Task task = null;
        if(optional.isPresent()) {
            task = optional.get();
        } else {
            throw new RuntimeException("The task with id " + id + " was not found.");
        }
        return task;
         */
    }

    @Override
    public void deleteTaskByID(Long id) {
        taskRepository.deleteById(id);
    }
}
