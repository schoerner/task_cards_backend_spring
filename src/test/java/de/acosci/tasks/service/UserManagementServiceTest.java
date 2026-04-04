package de.acosci.tasks.service;

import de.acosci.tasks.model.dto.UserManagementCreateDTO;
import de.acosci.tasks.model.dto.UserManagementUpdateDTO;
import de.acosci.tasks.model.entity.Role;
import de.acosci.tasks.model.entity.User;
import de.acosci.tasks.repository.RoleRepository;
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
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = {UserManagementServiceImpl.class})
class UserManagementServiceTest {

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RoleRepository roleRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserManagementServiceImpl userManagementService;

    @Test
    void createUser_shouldEncodePasswordAndPersistRoles() {
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
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User createdUser = userManagementService.createUser(dto);

        assertEquals("admin@test.de", createdUser.getEmail());
        assertEquals("encoded-secret", createdUser.getPassword());
        assertEquals("Ada", createdUser.getFirstName());
        assertEquals("Lovelace", createdUser.getLastName());
        assertEquals(2, createdUser.getRoles().size());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_shouldKeepPasswordWhenEmpty() {
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
                "",
                "New",
                "Name",
                Set.of(Role.RoleName.ROLE_USER)
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(roleRepository.findByName(Role.RoleName.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updatedUser = userManagementService.updateUser(1L, dto);

        assertEquals("new@test.de", updatedUser.getEmail());
        assertEquals("encoded-old", updatedUser.getPassword());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void getUserById_shouldThrowWhenMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userManagementService.getUserById(99L));
    }
}