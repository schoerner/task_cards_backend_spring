package de.acosci.tasks.repository;

import de.acosci.tasks.model.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    // https://spring.io/blog/2014/07/15/spel-support-in-spring-data-jpa-query-definitions
    /*@Query("DELETE FROM project_users pu WHERE pu.project_id = :#{#project.id}")
    @Modifying
    void delete(@Param("project") Project project);
     */
}
