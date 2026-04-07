package de.acosci.tasks.service;

import de.acosci.tasks.model.dto.ProjectMemberUpdateDTO;
import de.acosci.tasks.model.entity.ProjectMember;

import java.util.List;

/**
 * Service for project membership and project roles.
 */
public interface ProjectMembershipService {

    List<ProjectMember> getProjectMembers(Long projectId);

    ProjectMember addMember(Long projectId, ProjectMemberUpdateDTO dto);

    ProjectMember updateMemberRole(Long projectId, Long memberUserId, ProjectMemberUpdateDTO dto);

    void removeMember(Long projectId, Long memberUserId);
}