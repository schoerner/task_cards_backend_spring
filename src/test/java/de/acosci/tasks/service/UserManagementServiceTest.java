package de.acosci.tasks.service;

import de.acosci.tasks.model.dto.UserManagementCreateDTO;
import de.acosci.tasks.model.dto.UserManagementUpdateDTO;
import de.acosci.tasks.model.dto.UserResponseDTO;
import de.acosci.tasks.model.entity.Role;
import de.acosci.tasks.model.entity.User;
import de.acosci.tasks.model.entity.UserProfile;
import de.acosci.tasks.repository.RoleRepository;
import de.acosci.tasks.repository.UserProfileRepository;
import de.acosci.tasks.repository.UserRepository;
import de.acosci.tasks.service.impl.UserManagementServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Date;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        classes = {UserManagementServiceImpl.class}
)
class UserManagementServiceTest {

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RoleRepository roleRepository;

    @MockitoBean
    private UserProfileRepository userProfileRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserManagementServiceImpl userManagementService;

    @Test
    void createUser_shouldEncodePasswordPersistRolesAndCreateProfile() {
        Role adminRole = new Role(1L, Role.RoleName.ROLE_ADMIN);
        Role userRole = new Role(2L, Role.RoleName.ROLE_USER);

        UserManagementCreateDTO dto = new UserManagementCreateDTO(
                "admin@test.de",
                "secret",
                "Ada",
                "Lovelace",
                Set.of(Role.RoleName.ROLE_ADMIN, Role.RoleName.ROLE_USER)
        );

        when(passwordEncoder.encode("secret")).thenReturn("encoded-secret");
        when(roleRepository.findByName(Role.RoleName.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));
        when(roleRepository.findByName(Role.RoleName.ROLE_USER)).thenReturn(Optional.of(userRole));

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(10L);
            return user;
        });

        when(userProfileRepository.existsById(10L)).thenReturn(false);

        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(userRepository.findById(10L)).thenAnswer(invocation -> {
            User user = new User();
            user.setId(10L);
            user.setEmail("admin@test.de");
            user.setFirstName("Ada");
            user.setLastName("Lovelace");
            user.setRegistration(new Date());
            user.setRoles(Set.of(adminRole, userRole));
            return Optional.of(user);
        });

        UserResponseDTO createdUser = userManagementService.createUser(dto);

        assertNotNull(createdUser);
        assertEquals(10L, createdUser.getId());
        assertEquals("admin@test.de", createdUser.getEmail());
        assertEquals("Ada", createdUser.getFirstName());
        assertEquals("Lovelace", createdUser.getLastName());
        assertNotNull(createdUser.getRoles());
        assertEquals(2, createdUser.getRoles().size());

        verify(passwordEncoder).encode("secret");
        verify(roleRepository).findByName(Role.RoleName.ROLE_ADMIN);
        verify(roleRepository).findByName(Role.RoleName.ROLE_USER);
        verify(userRepository).save(argThat(user ->
                user.getEmail().equals("admin@test.de")
                        && user.getPassword().equals("encoded-secret")
                        && user.getRoles().size() == 2
        ));
        verify(userProfileRepository).existsById(10L);
        verify(userProfileRepository).save(any(UserProfile.class));
        verify(userRepository).findById(10L);
    }

    @Test
    void createUser_shouldThrowWhenNoRolesSelected() {
        UserManagementCreateDTO dto = new UserManagementCreateDTO(
                "user@test.de",
                "secret",
                "Test",
                "User",
                Set.of()
        );

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userManagementService.createUser(dto)
        );

        assertEquals("Mindestens eine Rolle muss ausgewählt werden.", ex.getMessage());

        verify(userRepository, never()).save(any(User.class));
        verify(userProfileRepository, never()).save(any(UserProfile.class));
    }

    @Test
    void updateUser_shouldKeepPasswordWhenEmptyAndUpdateRoles() {
        Role userRole = new Role(2L, Role.RoleName.ROLE_USER);
        Role adminRole = new Role(1L, Role.RoleName.ROLE_ADMIN);

        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("old@test.de");
        existingUser.setPassword("encoded-old");
        existingUser.setFirstName("Old");
        existingUser.setLastName("Name");
        existingUser.setRegistration(new Date());
        existingUser.setRoles(Set.of(userRole));

        UserManagementUpdateDTO dto = new UserManagementUpdateDTO(
                "new@test.de",
                "",
                "New",
                "Name",
                Set.of(Role.RoleName.ROLE_ADMIN, Role.RoleName.ROLE_USER)
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(existingUser))
                .thenAnswer(invocation -> {
                    User user = new User();
                    user.setId(1L);
                    user.setEmail("new@test.de");
                    user.setFirstName("New");
                    user.setLastName("Name");
                    user.setRegistration(existingUser.getRegistration());
                    user.setRoles(Set.of(userRole, adminRole));
                    return Optional.of(user);
                });

        when(roleRepository.findByName(Role.RoleName.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(roleRepository.findByName(Role.RoleName.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userProfileRepository.existsById(1L)).thenReturn(true);

        UserResponseDTO updatedUser = userManagementService.updateUser(1L, dto);

        assertNotNull(updatedUser);
        assertEquals("new@test.de", updatedUser.getEmail());
        assertEquals("New", updatedUser.getFirstName());
        assertEquals("Name", updatedUser.getLastName());
        assertNotNull(updatedUser.getRoles());
        assertEquals(2, updatedUser.getRoles().size());

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository).save(argThat(user ->
                user.getId().equals(1L)
                        && user.getEmail().equals("new@test.de")
                        && user.getPassword().equals("encoded-old")
                        && user.getRoles().size() == 2
        ));
        verify(userProfileRepository).existsById(1L);
        verify(userProfileRepository, never()).save(any(UserProfile.class));
        verify(userRepository, times(2)).findById(1L);
    }

    @Test
    void updateUser_shouldEncodePasswordWhenProvided() {
        Role userRole = new Role(2L, Role.RoleName.ROLE_USER);

        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("old@test.de");
        existingUser.setPassword("encoded-old");
        existingUser.setFirstName("Old");
        existingUser.setLastName("Name");
        existingUser.setRegistration(new Date());
        existingUser.setRoles(Set.of(userRole));

        UserManagementUpdateDTO dto = new UserManagementUpdateDTO(
                "new@test.de",
                "newSecret",
                "New",
                "Name",
                Set.of(Role.RoleName.ROLE_USER)
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(existingUser))
                .thenAnswer(invocation -> {
                    User user = new User();
                    user.setId(1L);
                    user.setEmail("new@test.de");
                    user.setFirstName("New");
                    user.setLastName("Name");
                    user.setRegistration(existingUser.getRegistration());
                    user.setRoles(Set.of(userRole));
                    return Optional.of(user);
                });

        when(roleRepository.findByName(Role.RoleName.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("newSecret")).thenReturn("encoded-new-secret");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userProfileRepository.existsById(1L)).thenReturn(true);

        UserResponseDTO updatedUser = userManagementService.updateUser(1L, dto);

        assertNotNull(updatedUser);
        assertEquals("new@test.de", updatedUser.getEmail());

        verify(passwordEncoder).encode("newSecret");
        verify(userRepository).save(argThat(user ->
                user.getId().equals(1L)
                        && user.getPassword().equals("encoded-new-secret")
        ));
    }

    @Test
    void getUserById_shouldThrowWhenMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userManagementService.getUserById(99L));
    }

    @Test
    void deleteUserById_shouldDeleteExistingUser() {
        User existingUser = new User();
        existingUser.setId(5L);

        when(userRepository.findById(5L)).thenReturn(Optional.of(existingUser));
        doNothing().when(userRepository).deleteById(5L);

        userManagementService.deleteUserById(5L);

        verify(userRepository).findById(5L);
        verify(userRepository).deleteById(5L);
    }
}