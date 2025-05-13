package de.acosci.tasks.service;

import de.acosci.tasks.model.Task;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class RAMTaskService implements TaskService {
    private static Long nextID = 1L;
    private final List<Task> tasks = new ArrayList<>();

    @Override
    public List<Task> getTasks() {
        return tasks;
    }

    @Override public List<Task> getTasksByUserID(Long userID) {
        tasks.clear();
        Iterator<Task> it = tasks.iterator();
        while(it.hasNext()) {
            Task t = it.next();
            if(t.getCreator().getId().equals(userID)) {
                tasks.add(t);
            }
        }
        return tasks;
    }

    @Override
    public Task saveTask(Task task) {
        task.setId(nextID);
        nextID++;
        tasks.add(task);
        return task;
    }

    @Override
    public Task getTaskByID(Long id) {
        Iterator<Task> it = tasks.iterator();
        while(it.hasNext()) {
            Task t = it.next();
            if(t.getId().equals(id)) {
                return t;
            }
        }
        throw new RuntimeException("The task with id " + id + " was not found.");
    }

    @Override
    public void deleteTaskByID(Long id) {
        Iterator<Task> it = tasks.iterator();
        while(it.hasNext()) {
            Task t = it.next();
            if(t.getId().equals(id)) {
                tasks.remove(t);
                return;
            }
        }
        throw new RuntimeException("The task with id " + id + " was not found.");
    }
}
