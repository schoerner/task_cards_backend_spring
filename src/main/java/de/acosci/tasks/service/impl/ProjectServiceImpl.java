package de.acosci.tasks.service.impl;

import de.acosci.tasks.model.dto.ProjectCreateDTO;
import de.acosci.tasks.model.dto.ProjectUpdateDTO;
import de.acosci.tasks.model.entity.BoardColumn;
import de.acosci.tasks.model.entity.Project;
import de.acosci.tasks.model.entity.ProjectMember;
import de.acosci.tasks.model.entity.User;
import de.acosci.tasks.model.enums.BoardColumnType;
import de.acosci.tasks.model.enums.ProjectRole;
import de.acosci.tasks.repository.ProjectMemberRepository;
import de.acosci.tasks.repository.ProjectRepository;
import de.acosci.tasks.repository.UserRepository;
import de.acosci.tasks.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectServiceImpl implements ProjectService {

    private static final String DEFAULT_NOT_ASSIGNED_COLUMN_NAME = "To Do";

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Project> getProjectsVisibleForCurrentUser() {
        User currentUser = getCurrentUser();

        return projectMemberRepository.findAllByUser_Id(currentUser.getId())
                .stream()
                .map(ProjectMember::getProject)
                .distinct()
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Project getVisibleProjectById(Long projectId) {
        User currentUser = getCurrentUser();

        return projectMemberRepository.findByProject_IdAndUser_Id(projectId, currentUser.getId())
                .map(ProjectMember::getProject)
                .orElseThrow(() -> new IllegalArgumentException("Project not visible for current user: " + projectId));
    }

    @Override
    public Project createProject(ProjectCreateDTO dto) {
        User currentUser = getCurrentUser();

        Project project = new Project();
        project.setName(dto.getName());
        project.setDescription(dto.getDescription());
        project.setCreator(currentUser);
        project = projectRepository.save(project);

        ProjectMember ownerMembership = new ProjectMember();
        ownerMembership.setProject(project);
        ownerMembership.setUser(currentUser);
        ownerMembership.setRole(ProjectRole.OWNER);
        projectMemberRepository.save(ownerMembership);

        initializeDefaultBoardColumns(project);
        return project;
    }

    @Override
    public Project updateProject(Long projectId, ProjectUpdateDTO dto) {
        Project project = getManageableProjectById(projectId);

        project.setName(dto.getName());
        project.setDescription(dto.getDescription());
        project.setArchived(dto.isArchived());

        return projectRepository.save(project);
    }

    @Override
    public Project archiveProject(Long projectId) {
        Project project = getManageableProjectById(projectId);
        project.setArchived(true);
        return projectRepository.save(project);
    }

    @Override
    public void deleteProject(Long projectId) {
        Project project = getManageableProjectById(projectId);
        projectRepository.delete(project);
    }


    private void initializeDefaultBoardColumns(Project project) {
        project.getBoardColumns().add(createSystemBoardColumn(project, DEFAULT_NOT_ASSIGNED_COLUMN_NAME, 0));
        project.getBoardColumns().add(createCustomBoardColumn(project, "In Progress", 2));
        project.getBoardColumns().add(createCustomBoardColumn(project, "Done", 3));
    }

    private BoardColumn createSystemBoardColumn(Project project, String name, int position) {
        BoardColumn column = createCustomBoardColumn(project, name, position);
        column.setType(BoardColumnType.SYSTEM);
        column.setDeletable(false);
        return column;
    }

    private BoardColumn createCustomBoardColumn(Project project, String name, int position) {
        BoardColumn column = new BoardColumn();
        column.setProject(project);
        column.setName(name);
        column.setPosition(position);
        column.setDeletable(true);
        return column;
    }

    private Project getManageableProjectById(Long projectId) {
        User currentUser = getCurrentUser();

        ProjectMember membership = projectMemberRepository.findByProject_IdAndUser_Id(projectId, currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Project not visible for current user: " + projectId));

        if (membership.getRole() != ProjectRole.OWNER && membership.getRole() != ProjectRole.MAINTAINER) {
            throw new IllegalArgumentException("Project not manageable for current user: " + projectId);
        }

        return membership.getProject();
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + email));
    }
}