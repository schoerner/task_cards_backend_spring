package de.acosci.tasks.service.impl;

import de.acosci.tasks.model.entity.Project;
import de.acosci.tasks.repository.ProjectRepository;
import de.acosci.tasks.service.ProjectService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectServiceImpl implements ProjectService {
    @Autowired
    private ProjectRepository projectRepository;

    @Override
    public List<Project> findAll() {
        return projectRepository.findAll();
    }

    @Override
    public Project findById(Long id) throws EntityNotFoundException {
        return projectRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("The project with id " + id + " was not found."));
    }

    @Override
    public Project save(Project project) {
        return projectRepository.save(project);
    }

    @Override
    public void delete(Project project) {
        projectRepository.delete(project);
    }
}
