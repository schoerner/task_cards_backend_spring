package de.acosci.tasks.controller.rest;

import de.acosci.tasks.model.entity.Task;
import de.acosci.tasks.service.TaskService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("api/tasks")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost", "http://localhost:5173/", "http://localhost:3000/"})
public class TaskRestController {
    @Autowired
    private final TaskService taskService;

    @GetMapping("/start/{id}")
    public ResponseEntity<Task> startTask(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(taskService.startTask(id));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/start")
    public ResponseEntity<Task> startTask(@RequestBody Task task) {
        return ResponseEntity.ok(taskService.startTask(task));
    }

    @GetMapping("/stop/{id}")
    public ResponseEntity<Task> stopTask(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(taskService.stopTask(id));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/stop")
    public ResponseEntity<Task> stopTask(@RequestBody Task task) {
        return ResponseEntity.ok(taskService.stopTask(task));
    }

    //@CrossOrigin(origins = "http://localhost:5173")
    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        try {
            return new ResponseEntity<>(taskService.getAllTasks(), HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Task>> getAllTasksByUserID(@PathVariable Long userId) {
        try {
            return new ResponseEntity<>(taskService.getAllTasksByUserID(userId), HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

//@CrossOrigin(origins = "http://localhost:5173")
@GetMapping("/{id}")
public ResponseEntity<Task> getTaskByID(@PathVariable Long id) {
    try {
        return new ResponseEntity<Task>(taskService.getTaskByID(id), HttpStatus.OK);
    } catch (EntityNotFoundException e) {
        return ResponseEntity.notFound().build();
    }
}

@PostMapping
public ResponseEntity<Task> saveNewTask(@RequestBody Task task) {
    try {
        return new ResponseEntity<Task>(taskService.saveTask(task), HttpStatus.OK);
    } catch(Exception e) { // todo
        return ResponseEntity.badRequest().build();
    }
}

@PutMapping
public ResponseEntity<Task> updateTaskByID(@RequestBody Task task) {
    try {
        return new ResponseEntity<Task>(taskService.saveTask(task), HttpStatus.OK);
    } catch(Exception e) { // todo
        return ResponseEntity.badRequest().build();
    }
}

@DeleteMapping("/{id}")
public ResponseEntity deleteTaskByID(@PathVariable Long id) {
    try {
        taskService.deleteTaskByID(id);
        return new ResponseEntity<>(HttpStatus.OK);
    } catch(Exception e) { // todo
        return ResponseEntity.badRequest().build();
    }
}

    @DeleteMapping
    public ResponseEntity deleteTask(@RequestBody Task task) {
        try {
            taskService.deleteTask(task);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch(Exception e) { // todo
            return ResponseEntity.badRequest().build();
        }
    }

}
