package de.acosci.tasks.model.mapper;

import de.acosci.tasks.model.dto.ProjectMemberResponseDTO;
import de.acosci.tasks.model.entity.ProjectMember;
import de.acosci.tasks.model.entity.User;

public final class ProjectMemberMapper {

    private ProjectMemberMapper() {
    }

    public static ProjectMemberResponseDTO toResponseDTO(ProjectMember member) {
        ProjectMemberResponseDTO dto = new ProjectMemberResponseDTO();
        dto.setProjectId(member.getProject().getId());
        dto.setUserId(member.getUser().getId());
        dto.setName(resolveMemberName(member.getUser()));
        dto.setContactEmail(resolveMemberContactEmail(member.getUser()));
        dto.setRole(member.getRole());
        dto.setJoinedAt(member.getJoinedAt());
        return dto;
    }

    private static String resolveMemberName(User user) {
        if (user == null) {
            return null;
        }

        if (user.getProfile() != null) {
            if (hasText(user.getProfile().getName())) {
                return user.getProfile().getName();
            }
            if (hasText(user.getProfile().getName())) {
                return user.getProfile().getName();
            }
        }

        String fullName = joinNonBlank(user.getFirstName(), user.getLastName());
        if (hasText(fullName)) {
            return fullName;
        }

        return "User " + user.getId();
    }

    private static String resolveMemberContactEmail(User user) {
        if (user == null) {
            return null;
        }

        if (user.getProfile() != null && hasText(user.getProfile().getContactEmail())) {
            return user.getProfile().getContactEmail();
        }

        return user.getEmail();
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static String joinNonBlank(String... values) {
        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            if (hasText(value)) {
                if (!builder.isEmpty()) {
                    builder.append(' ');
                }
                builder.append(value.trim());
            }
        }
        return builder.toString();
    }
}