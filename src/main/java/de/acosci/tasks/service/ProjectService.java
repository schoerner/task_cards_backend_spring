package de.acosci.tasks.service;

import de.acosci.tasks.model.entity.Project;
import de.acosci.tasks.model.entity.Task;

import java.util.List;

public interface ProjectService {
    List<Project> findAll();
    List<Project> getAllProjectsByUsersID(Long userID);
    List<Project> getAllProjectsByCreatorID(Long userID);
    Project findById(Long id);
    Project save(Project project);
    void deleteById(Long id);
    void delete(Project project);
}
