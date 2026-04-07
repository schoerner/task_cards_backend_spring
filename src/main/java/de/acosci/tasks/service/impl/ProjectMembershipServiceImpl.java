package de.acosci.tasks.service.impl;

import de.acosci.tasks.model.dto.ProjectMemberUpdateDTO;
import de.acosci.tasks.model.entity.Project;
import de.acosci.tasks.model.entity.ProjectMember;
import de.acosci.tasks.model.entity.User;
import de.acosci.tasks.model.enums.ProjectRole;
import de.acosci.tasks.repository.ProjectMemberRepository;
import de.acosci.tasks.repository.ProjectRepository;
import de.acosci.tasks.repository.UserRepository;
import de.acosci.tasks.service.ProjectMembershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectMembershipServiceImpl implements ProjectMembershipService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProjectMember> getProjectMembers(Long projectId) {
        Project project = getVisibleProject(projectId);
        return projectMemberRepository.findAllByProject_Id(project.getId());
    }

    @Override
    public ProjectMember addMember(Long projectId, ProjectMemberUpdateDTO dto) {
        Project project = getVisibleProject(projectId);

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + dto.getUserId()));

        ProjectMember membership = projectMemberRepository.findByProject_IdAndUser_Id(project.getId(), dto.getUserId())
                .orElseGet(ProjectMember::new);

        membership.setProject(project);
        membership.setUser(user);
        membership.setRole(dto.getRole());

        return projectMemberRepository.save(membership);
    }

    @Override
    public ProjectMember updateMemberRole(Long projectId, Long memberUserId, ProjectMemberUpdateDTO dto) {
        getVisibleProject(projectId);

        ProjectMember membership = projectMemberRepository.findByProject_IdAndUser_Id(projectId, memberUserId)
                .orElseThrow(() -> new IllegalArgumentException("Membership not found for projectId=" + projectId + ", userId=" + memberUserId));

        ProjectRole oldRole = membership.getRole();
        membership.setRole(dto.getRole());

        ensureAtLeastOneOwner(projectId, oldRole, dto.getRole());

        return projectMemberRepository.save(membership);
    }

    @Override
    public void removeMember(Long projectId, Long memberUserId) {
        getVisibleProject(projectId);

        ProjectMember membership = projectMemberRepository.findByProject_IdAndUser_Id(projectId, memberUserId)
                .orElseThrow(() -> new IllegalArgumentException("Membership not found for projectId=" + projectId + ", userId=" + memberUserId));

        ensureAtLeastOneOwner(projectId, membership.getRole(), null);

        projectMemberRepository.delete(membership);
    }

    private Project getVisibleProject(Long projectId) {
        User currentUser = getCurrentUser();

        if (!projectMemberRepository.existsByProject_IdAndUser_Id(projectId, currentUser.getId())) {
            throw new IllegalArgumentException("Project not visible for current user: " + projectId);
        }

        return projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));
    }

    private void ensureAtLeastOneOwner(Long projectId, ProjectRole oldRole, ProjectRole newRole) {
        if (oldRole != ProjectRole.OWNER) {
            return;
        }

        if (newRole == ProjectRole.OWNER) {
            return;
        }

        long ownerCount = projectMemberRepository.countByProject_IdAndRole(projectId, ProjectRole.OWNER);
        if (ownerCount <= 1) {
            throw new IllegalStateException("A project must always keep at least one OWNER.");
        }
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + email));
    }
}