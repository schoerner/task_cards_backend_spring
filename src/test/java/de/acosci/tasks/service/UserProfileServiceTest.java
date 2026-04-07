package de.acosci.tasks.service;

import de.acosci.tasks.model.dto.UserProfileResponseDTO;
import de.acosci.tasks.model.dto.UserProfileUpdateDTO;
import de.acosci.tasks.model.entity.User;
import de.acosci.tasks.model.entity.UserProfile;
import de.acosci.tasks.repository.UserProfileRepository;
import de.acosci.tasks.repository.UserRepository;
import de.acosci.tasks.service.impl.UserProfileServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserProfileServiceTest {

    private UserRepository userRepository;
    private UserProfileRepository userProfileRepository;
    private UserProfileServiceImpl userProfileService;

    private User user;
    private UserProfile profile;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userProfileRepository = mock(UserProfileRepository.class);
        userProfileService = new UserProfileServiceImpl(userRepository, userProfileRepository);

        user = new User();
        user.setId(1L);
        user.setEmail("test@test.de");
        user.setFirstName("Max");
        user.setLastName("Mustermann");

        profile = new UserProfile();
        profile.setId(1L);
        profile.setUser(user);
        profile.setName("Max Mustermann");
        profile.setContactEmail("test@test.de");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getEmail(), null, Collections.emptyList())
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getOwnProfile_returnsExistingProfile() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userProfileRepository.findByUser_Id(user.getId())).thenReturn(Optional.of(profile));

        UserProfileResponseDTO result = userProfileService.getOwnProfile();

        assertEquals("Max Mustermann", result.getName());
        verify(userProfileRepository, never()).save(any());
    }

    @Test
    void getOwnProfile_createsProfileIfMissing() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userProfileRepository.findByUser_Id(user.getId())).thenReturn(Optional.empty());
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserProfileResponseDTO result = userProfileService.getOwnProfile();

        assertEquals("Max Mustermann", result.getName());
        assertEquals("test@test.de", result.getContactEmail());
        verify(userProfileRepository).save(any(UserProfile.class));
    }

    @Test
    void updateOwnProfile_updatesFields() {
        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        dto.setName("Display Name");
        dto.setContactEmail("kontakt@test.de");
        dto.setPictureUrl("https://example.com/avatar.png");
        dto.setDescription("Hallo Welt");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userProfileRepository.findByUser_Id(user.getId())).thenReturn(Optional.of(profile));
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserProfileResponseDTO result = userProfileService.updateOwnProfile(dto);

        assertEquals("Display Name", result.getName());
        assertEquals("kontakt@test.de", result.getContactEmail());
        assertEquals("https://example.com/avatar.png", result.getPictureUrl());
        assertEquals("Hallo Welt", result.getDescription());
    }
}
