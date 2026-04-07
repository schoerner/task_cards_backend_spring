package de.acosci.tasks.model.mapper;

import de.acosci.tasks.model.dto.ProjectResponseDTO;
import de.acosci.tasks.model.entity.Project;

public final class ProjectMapper {

    private ProjectMapper() {
    }

    public static ProjectResponseDTO toProjectResponseDTO(Project project) {
        if (project == null) {
            return null;
        }

        ProjectResponseDTO dto = new ProjectResponseDTO();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setDescription(project.getDescription());
        dto.setArchived(project.isArchived());
        dto.setCreatedAt(project.getCreatedAt());
        dto.setUpdatedAt(project.getUpdatedAt());
        return dto;
    }
}