package de.acosci.tasks.repository;

import de.acosci.tasks.model.entity.Role;
import de.acosci.tasks.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    //@Query("SELECT u FROM User u WHERE u.email = ?1")
    Optional<User> findByEmail(String email);
    boolean existsByRoles_Name(Role.RoleName roleName);
}
