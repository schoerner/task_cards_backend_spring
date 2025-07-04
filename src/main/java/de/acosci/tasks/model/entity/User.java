package de.acosci.tasks.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Entity
@Table(name="users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", unique=true)
    private String email;

    @Column(name = "registration")
    @CreationTimestamp
    private Date registration;

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

    @OneToMany(mappedBy = "creator")
    @JsonIgnore
    private List<Task> tasks;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getUsername() {
        return email;
    }

    /* https://www.youtube.com/watch?v=CvDS6DltIno

    todo
    @ManyToMany
    @JoinTable(
        name = "user_permissions",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions;
    */
}