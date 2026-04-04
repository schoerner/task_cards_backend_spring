package de.acosci.tasks.common.runner;

import de.acosci.tasks.common.config.AdminConfig;
import de.acosci.tasks.model.entity.Role;
import de.acosci.tasks.model.entity.User;
import de.acosci.tasks.repository.RoleRepository;
import de.acosci.tasks.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
@Component
@RequiredArgsConstructor
public class InitialDataLoader implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminConfig adminConfig;

    @Override
    public void run(String... args) {
        // Seed roles
        createRoleIfNotExists(Role.RoleName.ROLE_USER);
        createRoleIfNotExists(Role.RoleName.ROLE_MODERATOR);
        createRoleIfNotExists(Role.RoleName.ROLE_ADMIN);

        // Seed Admin user
        String adminEmail = adminConfig.getEmail();
        String adminPassword = adminConfig.getPassword();

        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setFirstName("System");
            admin.setLastName("Admin");
            admin.setRegistration(new Date());

            Set<Role> roles = new HashSet<>();
            roles.add(roleRepository.findByName(Role.RoleName.ROLE_ADMIN).orElseThrow(
                    () -> new RuntimeException("Role not found")
            ));
            roles.add(roleRepository.findByName(Role.RoleName.ROLE_MODERATOR).orElseThrow(
                    () -> new RuntimeException("Role not found")
            ));
            roles.add(roleRepository.findByName(Role.RoleName.ROLE_USER).orElseThrow(
                    () -> new RuntimeException("Role not found")
            ));
            admin.setRoles(roles);

            userRepository.save(admin);
            System.out.printf("✅ Admin user created: %s / %s%n", adminEmail, adminPassword);
        } else {
            System.out.println("ℹ️ Admin user already exists");
        }
    }

    private void createRoleIfNotExists(Role.RoleName roleName) {
        roleRepository.findByName(roleName).orElseGet(() -> {
            Role role = new Role();
            role.setName(roleName);
            return roleRepository.save(role);
        });
    }
}
