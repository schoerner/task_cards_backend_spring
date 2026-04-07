package de.acosci.tasks.service.impl;

import de.acosci.tasks.model.dto.UserManagementCreateDTO;
import de.acosci.tasks.model.dto.UserManagementUpdateDTO;
import de.acosci.tasks.model.dto.UserResponseDTO;
import de.acosci.tasks.model.entity.Role;
import de.acosci.tasks.model.entity.User;
import de.acosci.tasks.model.entity.UserProfile;
import de.acosci.tasks.model.mapper.UserMapper;
import de.acosci.tasks.repository.RoleRepository;
import de.acosci.tasks.repository.UserProfileRepository;
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
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<UserResponseDTO> getUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserResponseDTO)
                .toList();
    }

    @Override
    public UserResponseDTO getUserById(Long id) {
        return UserMapper.toUserResponseDTO(getUserEntityById(id));
    }

    @Override
    public UserResponseDTO createUser(UserManagementCreateDTO dto) {
        validateRoleSelection(dto.getRoles());

        User user = new User();
        user.setEmail(dto.getEmail().trim());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setFirstName(dto.getFirstName().trim());
        user.setLastName(dto.getLastName().trim());
        user.setRoles(resolveRoles(dto.getRoles()));

        User savedUser = userRepository.save(user);
        ensureProfileExists(savedUser);

        User reloadedUser = getUserEntityById(savedUser.getId());
        return UserMapper.toUserResponseDTO(reloadedUser);
    }

    @Override
    public UserResponseDTO updateUser(Long id, UserManagementUpdateDTO dto) {
        validateRoleSelection(dto.getRoles());

        User user = getUserEntityById(id);
        user.setEmail(dto.getEmail().trim());
        user.setFirstName(dto.getFirstName().trim());
        user.setLastName(dto.getLastName().trim());
        user.setRoles(resolveRoles(dto.getRoles()));

        if (StringUtils.hasText(dto.getPassword())) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        User savedUser = userRepository.save(user);
        ensureProfileExists(savedUser);

        User reloadedUser = getUserEntityById(savedUser.getId());
        return UserMapper.toUserResponseDTO(reloadedUser);
    }

    @Override
    public void deleteUserById(Long id) {
        getUserEntityById(id);
        userRepository.deleteById(id);
    }

    private User getUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Der Benutzer mit der ID " + id + " wurde nicht gefunden."));
    }

    private Set<Role> resolveRoles(Set<Role.RoleName> roleNames) {
        Set<Role> roles = new HashSet<>();

        for (Role.RoleName roleName : roleNames) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new EntityNotFoundException("Die Rolle " + roleName + " wurde nicht gefunden."));
            roles.add(role);
        }

        return roles;
    }

    private void validateRoleSelection(Set<Role.RoleName> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            throw new IllegalArgumentException("Mindestens eine Rolle muss ausgewählt werden.");
        }
    }

    private void ensureProfileExists(User user) {
        if (user == null || user.getId() == null) {
            return;
        }

        if (!userProfileRepository.existsById(user.getId())) {
            UserProfile profile = new UserProfile();
            profile.setUser(user);
            profile.setName(buildDefaultDisplayName(user));
            profile.setContactEmail(user.getEmail());
            profile.setPictureUrl(null);
            profile.setDescription(null);
            userProfileRepository.save(profile);
            user.setProfile(profile);
        }
    }

    private String buildDefaultDisplayName(User user) {
        String firstName = user.getFirstName() != null ? user.getFirstName().trim() : "";
        String lastName = user.getLastName() != null ? user.getLastName().trim() : "";
        String fullName = (firstName + " " + lastName).trim();
        return fullName.isBlank() ? user.getEmail() : fullName;
    }
}