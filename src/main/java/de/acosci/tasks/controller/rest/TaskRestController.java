package de.acosci.tasks.controller.rest;

import de.acosci.tasks.model.entity.Task;
import de.acosci.tasks.service.TaskService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/tasks")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost", "http://localhost:5173/", "http://localhost:3000/"})
public class TaskRestController {

    private final TaskService taskService;

    // 🔹 GET alle Tasks des aktuellen Users/Admin
    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        try {
            return ResponseEntity.ok(taskService.getAllTasks());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
    @GetMapping("/my")
    public ResponseEntity<List<Task>> getMyTasks() {
        try {
            return new ResponseEntity<>(taskService.getMyTasks(), HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/my/owned")
    public ResponseEntity<List<Task>> getMyOwnedTasks() {
        try {
            return new ResponseEntity<>(taskService.getMyOwnedTasks(), HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 🔹 GET Task by ID
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(taskService.getTaskByID(id));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    // 🔹 POST Task anlegen (nur MODERATOR/ADMIN)
    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        try {
            Task saved = taskService.saveTask(task);
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 🔹 PUT Task updaten (REST-konform mit ID in URL)
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task task) {
        try {
            if (!id.equals(task.getId())) {
                return ResponseEntity.badRequest().build(); // Konsistenzcheck
            }
            Task updated = taskService.updateTask(task);
            return ResponseEntity.ok(updated);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    // 🔹 DELETE Task by ID (nur MODERATOR/ADMIN)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        try {
            taskService.deleteTaskByID(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    // 🔹 Start Task
    @PostMapping("/{id}/start")
    public ResponseEntity<Task> startTask(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(taskService.startTask(id));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    // 🔹 Stop Task
    @PostMapping("/{id}/stop")
    public ResponseEntity<Task> stopTask(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(taskService.stopTask(id));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
