package de.acosci.tasks.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Table(name="user")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", unique=true)
    private String email;

    @Column(name = "registration")
    @CreationTimestamp
    private LocalDateTime registration;

    @JsonIgnore
    @Column(name = "password")
    private String password;

    @JsonIgnore
    @Transient
    private String password_verification;

    @Column(name = "first_name", nullable = false, length = 20)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    public boolean arePasswordsMatching() {
        return password.equals(password_verification);
    }
    public boolean hasStrongPassword() {
        return password.length() >= 4;
    }

    /*
    @OneToMany(mappedBy = "creator")
    private List<Task> tasks;
*/
    /** https://www.youtube.com/watch?v=CvDS6DltIno */
    @ManyToMany
    @JoinTable(
        name = "user_permissions",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions;
}