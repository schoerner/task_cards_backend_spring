package de.acosci.tasks.controller.rest;

import de.acosci.tasks.model.entity.Project;
import de.acosci.tasks.service.ProjectService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/projects")
//@PreAuthorize("hasRole('MODERATOR')")  // Optional, filtered via FilterChain
@RequiredArgsConstructor
public class ProjectRestController {
    @Autowired
    private ProjectService projectService;

    @GetMapping
    public ResponseEntity<List<Project>> findAll() {
        try {
            return new ResponseEntity<>(projectService.findAll(), HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> getByID(@PathVariable Long id) {
        try {
            return new ResponseEntity<>(projectService.findById(id), HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Project>> getAllProjectsByUserID(@PathVariable Long userId) {
        try {
            return new ResponseEntity<>(projectService.getAllProjectsByUsersID(userId), HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/creator/{userId}")
    public ResponseEntity<List<Project>> getAllProjectsByCreatorD(@PathVariable Long userId) {
        try {
            return new ResponseEntity<>(projectService.getAllProjectsByCreatorID(userId), HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Project> save(@RequestBody Project project){
        try {
            return new ResponseEntity<>(projectService.save(project), HttpStatus.OK);
        } catch(Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping
    public ResponseEntity<Project> update(@RequestBody Project project){
        try {
            return new ResponseEntity<>(projectService.save(project), HttpStatus.OK);
        } catch(Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Project> deleteByID(@PathVariable Long id) {
        try {
            projectService.deleteById(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping
    public ResponseEntity<Project> delete(@RequestBody Project project) {
        try {
            projectService.delete(project);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch(Exception e) { // todo
            return ResponseEntity.badRequest().build();
        }
    }
}
