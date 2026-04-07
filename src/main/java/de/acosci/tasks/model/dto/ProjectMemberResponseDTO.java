package de.acosci.tasks.model.dto;

import de.acosci.tasks.model.enums.ProjectRole;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class ProjectMemberResponseDTO {

    private Long projectId;
    private Long userId;

    private String email;
    private String firstName;
    private String lastName;

    private ProjectRole role;
    private OffsetDateTime joinedAt;
}