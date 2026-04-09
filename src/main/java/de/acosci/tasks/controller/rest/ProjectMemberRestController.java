package de.acosci.tasks.controller.rest;

import de.acosci.tasks.model.dto.ProjectMemberResponseDTO;
import de.acosci.tasks.model.dto.ProjectMemberUpdateDTO;
import de.acosci.tasks.model.dto.UserProfileSummaryDTO;
import de.acosci.tasks.model.entity.ProjectMember;
import de.acosci.tasks.model.mapper.ProjectMemberMapper;
import de.acosci.tasks.service.ProjectMembershipService;
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
@RequestMapping("/api/v1/projects/{projectId}/members")
@RequiredArgsConstructor
@Tag(name = "Project Members", description = "Operations for project member management")
public class ProjectMemberRestController {

    private final ProjectMembershipService projectMembershipService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.isMemberByEmail(#projectId, authentication.name)")
    @Operation(summary = "Get all members of a project")
    public ResponseEntity<List<ProjectMemberResponseDTO>> getMembers(@PathVariable Long projectId) {
        List<ProjectMemberResponseDTO> result = projectMembershipService.getProjectMembers(projectId)
                .stream()
                .map(ProjectMemberMapper::toResponseDTO)
                .toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/candidates")
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.canManageMembersByEmail(#projectId, authentication.name)")
    @Operation(summary = "Search member candidates by profile name or contact email")
    public ResponseEntity<List<UserProfileSummaryDTO>> searchMemberCandidates(
            @PathVariable Long projectId,
            @RequestParam(required = false) String query
    ) {
        return ResponseEntity.ok(projectMembershipService.searchMemberCandidates(projectId, query));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.canManageMembersByEmail(#projectId, authentication.name)")
    @Operation(summary = "Add a member to a project")
    public ResponseEntity<ProjectMemberResponseDTO> addMember(
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectMemberUpdateDTO dto
    ) {
        ProjectMember member = projectMembershipService.addMember(projectId, dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProjectMemberMapper.toResponseDTO(member));
    }

    @PutMapping("/{memberUserId}")
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.canManageMembersByEmail(#projectId, authentication.name)")
    @Operation(summary = "Update member role")
    public ResponseEntity<ProjectMemberResponseDTO> updateMemberRole(
            @PathVariable Long projectId,
            @PathVariable Long memberUserId,
            @Valid @RequestBody ProjectMemberUpdateDTO dto
    ) {
        ProjectMember member = projectMembershipService.updateMemberRole(projectId, memberUserId, dto);
        return ResponseEntity.ok(ProjectMemberMapper.toResponseDTO(member));
    }

    @DeleteMapping("/{memberUserId}")
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.canManageMembersByEmail(#projectId, authentication.name)")
    @Operation(summary = "Remove project member")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long projectId,
            @PathVariable Long memberUserId
    ) {
        projectMembershipService.removeMember(projectId, memberUserId);
        return ResponseEntity.noContent().build();
    }
}
