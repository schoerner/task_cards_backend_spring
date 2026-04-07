package de.acosci.tasks.controller.rest;

import de.acosci.tasks.model.dto.ProjectCreateDTO;
import de.acosci.tasks.model.dto.ProjectResponseDTO;
import de.acosci.tasks.model.dto.ProjectUpdateDTO;
import de.acosci.tasks.model.entity.Project;
import de.acosci.tasks.model.mapper.ProjectMapper;
import de.acosci.tasks.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Tag(name = "Projects", description = "Operations for project management")
public class ProjectRestController {

    private final ProjectService projectService;

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Get all projects visible for the current user")
    public ResponseEntity<List<ProjectResponseDTO>> getVisibleProjects() {
        List<ProjectResponseDTO> result = projectService.getProjectsVisibleForCurrentUser()
                .stream()
                .map(ProjectMapper::toProjectResponseDTO)
                .toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{projectId}")
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.isMemberByEmail(#projectId, authentication.name)")
    @Operation(summary = "Get a project by id")
    public ResponseEntity<ProjectResponseDTO> getProject(@PathVariable Long projectId) {
        Project project = projectService.getVisibleProjectById(projectId);
        return ResponseEntity.ok(ProjectMapper.toProjectResponseDTO(project));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Create a new project")
    public ResponseEntity<ProjectResponseDTO> createProject(@Valid @RequestBody ProjectCreateDTO dto) {
        Project project = projectService.createProject(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProjectMapper.toProjectResponseDTO(project));
    }

    @PutMapping("/{projectId}")
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.canManageByEmail(#projectId, authentication.name)")
    @Operation(summary = "Update a project")
    public ResponseEntity<ProjectResponseDTO> updateProject(
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectUpdateDTO dto
    ) {
        Project project = projectService.updateProject(projectId, dto);
        return ResponseEntity.ok(ProjectMapper.toProjectResponseDTO(project));
    }

    @PatchMapping("/{projectId}/archive")
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.canManageByEmail(#projectId, authentication.name)")
    @Operation(summary = "Archive a project")
    public ResponseEntity<ProjectResponseDTO> archiveProject(@PathVariable Long projectId) {
        Project project = projectService.archiveProject(projectId);
        return ResponseEntity.ok(ProjectMapper.toProjectResponseDTO(project));
    }

    @DeleteMapping("/{projectId}")
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.canManageByEmail(#projectId, authentication.name)")
    @Operation(summary = "Delete a project")
    public ResponseEntity<Void> deleteProject(@PathVariable Long projectId) {
        projectService.deleteProject(projectId);
        return ResponseEntity.noContent().build();
    }
}