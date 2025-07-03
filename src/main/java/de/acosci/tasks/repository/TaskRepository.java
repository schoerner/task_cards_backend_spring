package de.acosci.tasks.repository;

import de.acosci.tasks.model.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Gernot Schörner
 * The @Repository annotation is a marker for any class that fulfills the role or stereotype of a repository (also known as Data Access Object or DAO). Among the uses of this marker is the automatic translation of exceptions, as described in Exception Translation.
 * Source: Layers: https://www.tpointtech.com/spring-boot-architecture
 * Source: https://docs.spring.io/spring-framework/reference/core/beans/classpath-scanning.html#beans-stereotype-annotations
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
}
