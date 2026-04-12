package de.acosci.tasks.common.runner;

import de.acosci.tasks.common.config.AdminConfig;
import de.acosci.tasks.model.entity.Role;
import de.acosci.tasks.model.entity.User;
import de.acosci.tasks.repository.RoleRepository;
import de.acosci.tasks.repository.UserRepository;
import de.acosci.tasks.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class InitialDataLoader implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final AdminConfig adminConfig;

    @Value("${app.initial-data.enabled:true}")
    private boolean initialDataEnabled;

    @Override
    public void run(String... args) {
        if (!initialDataEnabled) {
            System.out.println("ℹ️ InitialDataLoader disabled");
            return;
        }

        createRoleIfNotExists(Role.RoleName.ROLE_USER);
        createRoleIfNotExists(Role.RoleName.ROLE_ADMIN);

        boolean adminExists = userRepository.existsByRoles_Name(Role.RoleName.ROLE_ADMIN);
        if (adminExists) {
            System.out.println("ℹ️ Admin user already exists");
            return;
        }

        String adminEmail = adminConfig.getEmail();
        String adminPassword = adminConfig.getPassword();

        Role adminRole = roleRepository.findByName(Role.RoleName.ROLE_ADMIN).orElseThrow(
                () -> new RuntimeException("Role ROLE_ADMIN not found")
        );
        Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER).orElseThrow(
                () -> new RuntimeException("Role ROLE_USER not found")
        );

        User admin = new User();
        admin.setEmail(adminEmail);
        admin.setPassword(adminPassword);
        admin.setFirstName("System");
        admin.setLastName("Admin");
        admin.setRegistration(new Date());

        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);
        roles.add(userRole);
        admin.setRoles(roles);

        userService.saveUser(admin);
        System.out.printf("✅ Default admin user created: %s / %s%n", adminEmail, adminPassword);
    }

    private void createRoleIfNotExists(Role.RoleName roleName) {
        roleRepository.findByName(roleName).orElseGet(() -> {
            Role role = new Role();
            role.setName(roleName);
            return roleRepository.save(role);
        });
    }
}