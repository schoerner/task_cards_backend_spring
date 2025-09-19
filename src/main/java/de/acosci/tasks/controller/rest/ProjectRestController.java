package de.acosci.tasks.controller.rest;

import de.acosci.tasks.model.entity.Project;
import de.acosci.tasks.model.entity.User;
import de.acosci.tasks.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/projects")
//@PreAuthorize("hasRole('MODERATOR')")  // Optional, filtered via FilterChain
@RequiredArgsConstructor
public class ProjectRestController {
    private final ProjectService projectService;

    /** Member can access the projects they are participating on. */
    @GetMapping("/my")
    public ResponseEntity<List<Project>> getMyProjects() {
        /* Get the current, logged in user from the security context to avoid IDOR (Insecure Direct Object Reference)
         * access control vulnerability.
         */
        User current = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(projectService.getAllProjectsByMembersID(current.getId()));
    }

    /** Creators ar owners of projects, they get their projects here. */
    @GetMapping("/my-owned")
    public ResponseEntity<List<Project>> getMyOwnedProjects() {
        User current = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(projectService.getAllProjectsByCreatorID(current.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> getProject(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.findById(id));
    }

    /** Admins and moderators are able to create projects. */
    @PostMapping
    public ResponseEntity<Project> createProject(@RequestBody Project project) {
        // Creator wird im Service aus SecurityContext gesetzt
        return ResponseEntity.ok(projectService.save(project));
    }

    /** Update projects. */
    @PutMapping("/{id}")
    public ResponseEntity<Project> updateProject(@PathVariable Long id, @RequestBody Project project) {
        project.setId(id);
        return ResponseEntity.ok(projectService.save(project));
    }

    /** Delete projects by owners or admins. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
