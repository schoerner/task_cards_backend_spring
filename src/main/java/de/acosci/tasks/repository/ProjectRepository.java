package de.acosci.tasks.repository;

import de.acosci.tasks.model.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findAllByCreator_Id(Long creatorId);
    List<Project> findAllByArchivedFalse();
}
