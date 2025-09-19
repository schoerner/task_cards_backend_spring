package de.acosci.tasks.service.impl;

import de.acosci.tasks.model.entity.Project;
import de.acosci.tasks.model.entity.User;
import de.acosci.tasks.repository.ProjectRepository;
import de.acosci.tasks.service.ProjectService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

/** Rules
 * ROLE_USER:       Authorized to read projects, where it is member
 * ROLE_MODERATOR:  Authorized to create, update and delete own projects (creator) or read as member or owner.
 * ROLE_ADMIN:      Authorized to do all CRUD operations on all projects.
 */
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (User) auth.getPrincipal();
    }

    // -------------------------
    // READ
    // -------------------------

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN') " +
            "or hasRole('ROLE_MODERATOR') and (@projectSecurity.isOwner(#id, principal.id) or @projectSecurity.isMember(#id, principal.id)) " +
            "or hasRole('ROLE_USER') and @projectSecurity.isMember(#id, principal.id)")
    public Project findById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id " + id));
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN') or #userId == principal.id")
    public List<Project> getAllProjectsByCreatorID(Long userId) {
        return projectRepository.findByCreator_Id(userId);
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN') or #userId == principal.id")
    public List<Project> getAllProjectsByMembersID(Long userId) {
        return projectRepository.findByMembers_Id(userId);
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<Project> findAll() {
        return projectRepository.findAll();
    }

    // -------------------------
    // CREATE & UPDATE
    // -------------------------

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN') " +
            "or hasRole('ROLE_MODERATOR') and (#project.id == null or @projectSecurity.isOwner(#project.id, principal.id))")
    public Project save(Project project) {
        if (project.getId() == null) {
            // Creator automatisch setzen
            User current = getCurrentUser();
            project.setCreator(current);
        }
        return projectRepository.save(project);
    }

    // -------------------------
    // DELETE
    // -------------------------

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MODERATOR') and @projectSecurity.isOwner(#id, principal.id)")
    public void deleteById(Long id) {
        projectRepository.deleteById(id);
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MODERATOR') and @projectSecurity.isOwner(#project.id, principal.id)")
    public void delete(Project project) {
        projectRepository.delete(project);
    }
}
