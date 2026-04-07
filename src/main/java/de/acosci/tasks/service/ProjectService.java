package de.acosci.tasks.service;

import de.acosci.tasks.model.dto.ProjectCreateDTO;
import de.acosci.tasks.model.dto.ProjectUpdateDTO;
import de.acosci.tasks.model.entity.Project;

import java.util.List;

/**
 * Service for project lifecycle and read access.
 */
public interface ProjectService {

    List<Project> getProjectsVisibleForCurrentUser();

    Project getVisibleProjectById(Long projectId);

    Project createProject(ProjectCreateDTO dto);

    Project updateProject(Long projectId, ProjectUpdateDTO dto);

    Project archiveProject(Long projectId);

    void deleteProject(Long projectId);
}