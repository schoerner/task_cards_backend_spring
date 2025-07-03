package de.acosci.tasks.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="permission")
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name")
    private Boolean name;
    @Column(name = "description")
    private Boolean description;
/* todo
    @ManyToMany(mappedBy = "permissions")
    private List<User> users;
    */
}
