package de.acosci.tasks.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Entity
@Table(name="users")
// todo Info @Data Problem with Circular References
// <=> ManyToMany && JPA => StackOverflowError -> https://stackoverflow.com/questions/62585553/java-spring-boot-jpa-stackoverflowerror-with-a-manytomany-relation
// => Implement/generate toString, hashCode and equals
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    @CreationTimestamp
    private Date registration;

    @JsonIgnore
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

    @JsonIgnore
    @OneToMany(mappedBy = "creator")
    private List<Task> tasks = new ArrayList<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "users") // User is not the owner of the relation
    private Set<Project> projects = new HashSet<>();

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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(email, user.email) && Objects.equals(registration, user.registration) && Objects.equals(firstName, user.firstName) && Objects.equals(lastName, user.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email, registration, firstName, lastName);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", registration=" + registration +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }
}