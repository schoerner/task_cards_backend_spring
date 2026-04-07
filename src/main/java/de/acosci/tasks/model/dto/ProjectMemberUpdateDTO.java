package de.acosci.tasks.model.dto;

import de.acosci.tasks.model.enums.ProjectRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request payload for adding or changing a project member.
 */
@Data
public class ProjectMemberUpdateDTO {
    @NotNull
    private Long userId;

    @NotNull
    private ProjectRole role;
}
