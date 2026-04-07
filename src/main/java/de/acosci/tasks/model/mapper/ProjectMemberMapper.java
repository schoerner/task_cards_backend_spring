package de.acosci.tasks.model.mapper;

import de.acosci.tasks.model.dto.ProjectMemberResponseDTO;
import de.acosci.tasks.model.entity.ProjectMember;

public final class ProjectMemberMapper {

    private ProjectMemberMapper() {
    }

    public static ProjectMemberResponseDTO toResponseDTO(ProjectMember member) {
        if (member == null) {
            return null;
        }

        ProjectMemberResponseDTO dto = new ProjectMemberResponseDTO();

        dto.setProjectId(member.getProject() != null ? member.getProject().getId() : null);
        dto.setUserId(member.getUser() != null ? member.getUser().getId() : null);

        dto.setEmail(member.getUser() != null ? member.getUser().getEmail() : null);
        dto.setFirstName(member.getUser() != null ? member.getUser().getFirstName() : null);
        dto.setLastName(member.getUser() != null ? member.getUser().getLastName() : null);

        dto.setRole(member.getRole());
        dto.setJoinedAt(member.getJoinedAt());

        return dto;
    }
}