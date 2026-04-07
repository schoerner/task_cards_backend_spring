package de.acosci.tasks.service;

import de.acosci.tasks.model.dto.UserUpdateDTO;
import de.acosci.tasks.model.entity.User;
import de.acosci.tasks.model.entity.UserProfile;
import de.acosci.tasks.repository.UserProfileRepository;
import de.acosci.tasks.repository.UserRepository;
import de.acosci.tasks.service.impl.UserServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @Test
    void saveUser_createsProfileAndEncodesPassword_forNewUser() {
        UserRepository userRepository = mock(UserRepository.class);
        UserProfileRepository userProfileRepository = mock(UserProfileRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

        UserServiceImpl service = new UserServiceImpl(userRepository, userProfileRepository, passwordEncoder);

        User newUser = new User();
        newUser.setEmail("new@test.de");
        newUser.setPassword("Secret123");
        newUser.setFirstName("Ada");
        newUser.setLastName("Lovelace");

        when(passwordEncoder.encode("Secret123")).thenReturn("encoded-secret");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            if (saved.getId() == null) {
                saved.setId(10L);
            }
            return saved;
        });
        when(userProfileRepository.existsById(10L)).thenReturn(false);
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = service.saveUser(newUser);

        assertEquals("encoded-secret", result.getPassword());
        assertNotNull(result.getProfile());
        assertEquals("Ada Lovelace", result.getProfile().getName());
        assertEquals("new@test.de", result.getProfile().getContactEmail());
        verify(userProfileRepository).save(any(UserProfile.class));
    }

    @Test
    void updateUser_updatesOnlyAllowedFields() {
        UserRepository userRepository = mock(UserRepository.class);
        UserProfileRepository userProfileRepository = mock(UserProfileRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

        UserServiceImpl service = new UserServiceImpl(userRepository, userProfileRepository, passwordEncoder);

        User existing = new User();
        existing.setId(1L);
        existing.setEmail("old@test.de");
        existing.setFirstName("Old");
        existing.setLastName("Name");
        existing.setPassword("encoded-old");

        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setEmail("new@test.de");
        dto.setFirstName("New");
        dto.setLastName("Name");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = service.updateUser(1L, dto);

        assertEquals("new@test.de", result.getEmail());
        assertEquals("New", result.getFirstName());
        assertEquals("Name", result.getLastName());
        assertEquals("encoded-old", result.getPassword());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void updateUser_throwsWhenMissing() {
        UserRepository userRepository = mock(UserRepository.class);
        UserProfileRepository userProfileRepository = mock(UserProfileRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

        UserServiceImpl service = new UserServiceImpl(userRepository, userProfileRepository, passwordEncoder);

        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setEmail("missing@test.de");

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.updateUser(99L, dto));
    }
}
