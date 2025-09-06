package de.acosci.tasks.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "projects")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @ManyToOne
    @JoinColumn(name = "creator_user_id")
    private User creator;

    @JsonIgnore
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks = new ArrayList<>();

    // here no @JsonIgnore => to be able to add a user via JPA
    /* https://www.baeldung.com/jpa-remove-entity-many-to-many
     * this Object is the owner of the relationship
    java.sql.SQLException: (conn=26) Cannot delete or update a parent row: a foreign key constraint fails (`tasks`.`project_users`, CONSTRAINT `FKn2d9w5xxgord5j4k2963p8o1g` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`))
at org.mariadb.jdbc.export.ExceptionFactory.createException(ExceptionFactory.java:297)
     */
    @ManyToMany//(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(
            name = "project_users",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> users = new HashSet<>();
}
