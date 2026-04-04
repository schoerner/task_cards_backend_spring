package de.acosci.tasks.service.impl;

import de.acosci.tasks.model.dto.UserManagementCreateDTO;
import de.acosci.tasks.model.dto.UserManagementUpdateDTO;
import de.acosci.tasks.model.entity.Role;
import de.acosci.tasks.model.entity.User;
import de.acosci.tasks.repository.RoleRepository;
import de.acosci.tasks.repository.UserRepository;
import de.acosci.tasks.service.UserManagementService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("The user with id " + id + " was not found."));
    }

    @Override
    public User createUser(UserManagementCreateDTO dto) {
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setRoles(resolveRoles(dto.getRoles()));
        return userRepository.save(user);
    }

    @Override
    public User updateUser(Long id, UserManagementUpdateDTO dto) {
        User user = getUserById(id);
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setRoles(resolveRoles(dto.getRoles()));

        if (StringUtils.hasText(dto.getPassword())) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        return userRepository.save(user);
    }

    @Override
    public void deleteUserById(Long id) {
        getUserById(id);
        userRepository.deleteById(id);
    }

    private Set<Role> resolveRoles(Set<Role.RoleName> roleNames) {
        Set<Role> roles = new HashSet<>();

        for (Role.RoleName roleName : roleNames) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new EntityNotFoundException("Role " + roleName + " was not found."));
            roles.add(role);
        }

        return roles;
    }
}