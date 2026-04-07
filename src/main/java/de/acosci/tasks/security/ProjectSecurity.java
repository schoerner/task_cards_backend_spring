package de.acosci.tasks.security;

import de.acosci.tasks.model.enums.ProjectRole;
import de.acosci.tasks.repository.ProjectMemberRepository;
import de.acosci.tasks.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Project-scoped authorization helper for SpEL expressions.
 */
@Component("projectSecurity")
@RequiredArgsConstructor
public class ProjectSecurity {

    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    public boolean isMember(Long projectId, Long userId) {
        return projectMemberRepository.existsByProject_IdAndUser_Id(projectId, userId);
    }

    public boolean isMemberByEmail(Long projectId, String email) {
        return userRepository.findByEmail(email)
                .map(user -> isMember(projectId, user.getId()))
                .orElse(false);
    }

    public boolean hasRole(Long projectId, Long userId, ProjectRole role) {
        return projectMemberRepository.findByProject_IdAndUser_Id(projectId, userId)
                .map(member -> member.getRole() == role)
                .orElse(false);
    }

    public boolean hasRoleByEmail(Long projectId, String email, ProjectRole role) {
        return userRepository.findByEmail(email)
                .map(user -> hasRole(projectId, user.getId(), role))
                .orElse(false);
    }

    public boolean isOwner(Long projectId, Long userId) {
        return hasRole(projectId, userId, ProjectRole.OWNER);
    }

    public boolean isOwnerByEmail(Long projectId, String email) {
        return hasRoleByEmail(projectId, email, ProjectRole.OWNER);
    }

    public boolean isMaintainer(Long projectId, Long userId) {
        return hasRole(projectId, userId, ProjectRole.MAINTAINER);
    }

    public boolean isMaintainerByEmail(Long projectId, String email) {
        return hasRoleByEmail(projectId, email, ProjectRole.MAINTAINER);
    }

    public boolean canManage(Long projectId, Long userId) {
        return isMaintainer(projectId, userId) || isOwner(projectId, userId);
    }

    public boolean canManageByEmail(Long projectId, String email) {
        return userRepository.findByEmail(email)
                .map(user -> canManage(projectId, user.getId()))
                .orElse(false);
    }

    public boolean canManageBoard(Long projectId, Long userId) {
        return isMaintainer(projectId, userId) || isOwner(projectId, userId);
    }

    public boolean canManageBoardByEmail(Long projectId, String email) {
        return userRepository.findByEmail(email)
                .map(user -> canManageBoard(projectId, user.getId()))
                .orElse(false);
    }

    public boolean canManageMembers(Long projectId, Long userId) {
        return isMaintainer(projectId, userId) || isOwner(projectId, userId);
    }

    public boolean canManageMembersByEmail(Long projectId, String email) {
        return userRepository.findByEmail(email)
                .map(user -> canManageMembers(projectId, user.getId()))
                .orElse(false);
    }

    public boolean canEditTasks(Long projectId, Long userId) {
        return projectMemberRepository.findByProject_IdAndUser_Id(projectId, userId)
                .map(member -> member.getRole() != ProjectRole.VIEWER)
                .orElse(false);
    }

    public boolean canEditTasksByEmail(Long projectId, String email) {
        return userRepository.findByEmail(email)
                .map(user -> canEditTasks(projectId, user.getId()))
                .orElse(false);
    }
}