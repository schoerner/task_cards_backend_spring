package de.acosci.tasks.service;

import de.acosci.tasks.model.entity.Project;

import java.util.List;

public interface ProjectService {
    List<Project> findAll();
    Project findById(Long id);
    Project save(Project project);
    void delete(Project project);
}
