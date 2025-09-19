package de.acosci.tasks.repository;

import de.acosci.tasks.model.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    // https://spring.io/blog/2014/07/15/spel-support-in-spring-data-jpa-query-definitions
    /*@Query("DELETE FROM project_users pu WHERE pu.project_id = :#{#project.id}")
    @Modifying
    void delete(@Param("project") Project project);
     */
    List<Project> findByMembers_Id(Long userID);

    List<Project> findByCreator_Id(Long userID);
}
