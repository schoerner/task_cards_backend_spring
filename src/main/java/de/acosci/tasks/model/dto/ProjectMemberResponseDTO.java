package de.acosci.tasks.model.dto;

import de.acosci.tasks.model.enums.ProjectRole;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class ProjectMemberResponseDTO {

    private Long projectId;
    private Long userId;

    private String name;
    private String contactEmail;

    private ProjectRole role;
    private OffsetDateTime joinedAt;
}