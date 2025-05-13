package de.acosci.tasks.controller.rest;

import de.acosci.tasks.model.Task;
import de.acosci.tasks.model.User;
import de.acosci.tasks.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Source: https://spring.io/guides/tutorials/rest
 *
 * Cross-Origin Resource Sharing (CORS)
 * https://stackoverflow.com/questions/39623211/add-multiple-cross-origin-urls-in-spring-boot
 * https://www.youtube.com/watch?app=desktop&v=HRwlT_etr60
 */
@RestController
@RequestMapping("rest/tasks")
@RequiredArgsConstructor
@CrossOrigin("http://localhost:5173/")//@CrossOrigin(origins = {"http://localhost:5173, http://localhost:3030"}) // vue frontend: http://localhost:5173, postman: http://localhost:3030
public class TaskRestController {
    @Autowired
    private final TaskService taskService;

    //@CrossOrigin(origins = "http://localhost:5173")
    @GetMapping
    public List<Task> getAllTasks() {
        return taskService.getTasks();
    }

    //@CrossOrigin(origins = "http://localhost:5173")
    @GetMapping("/{id}")
    public Task getTaskByID(@PathVariable Long id) {
        return taskService.getTaskByID(id);
    }

    @PostMapping("/add")
    public Task saveNewTask(@RequestBody Task newTask) {
        System.out.println("post mapping reached");
        return taskService.saveTask(newTask);
    }

    @PutMapping("/edit/{id}")
    public Task updateTaskByID(@RequestBody Task taskToBeUpdated, @PathVariable Long id) {
        System.out.println("put mapping reached");
        return taskService.saveTask(taskToBeUpdated);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteTaskByID(@PathVariable Long id) {
        System.out.println("delete mapping reached");
        taskService.deleteTaskByID(id);
    }

}
