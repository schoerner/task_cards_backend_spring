package de.acosci.tasks.service;

import de.acosci.tasks.model.dto.UserProfileResponseDTO;
import de.acosci.tasks.model.dto.UserProfileSummaryDTO;
import de.acosci.tasks.model.dto.UserProfileUpdateDTO;
import de.acosci.tasks.model.entity.User;
import de.acosci.tasks.model.entity.UserProfile;
import de.acosci.tasks.repository.UserProfileRepository;
import de.acosci.tasks.repository.UserRepository;
import de.acosci.tasks.service.impl.UserProfileServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserProfileServiceTest {

    private final UserRepository userRepository = Mockito.mock(UserRepository.class);
    private final UserProfileRepository userProfileRepository = Mockito.mock(UserProfileRepository.class);

    private final UserProfileServiceImpl service = new UserProfileServiceImpl(userRepository, userProfileRepository);

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("getOwnProfile creates a profile from current user when none exists")
    void getOwnProfile_createsProfileWhenMissing() {
        User user = createUser(5L, "alice@example.com", "Alice", "Example");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("alice@example.com", "pw")
        );

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(userProfileRepository.findByUser_Id(5L)).thenReturn(Optional.empty());
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> {
            UserProfile saved = invocation.getArgument(0);
            saved.setId(5L);
            return saved;
        });

        UserProfileResponseDTO result = service.getOwnProfile();

        assertNotNull(result);
        assertEquals(5L, result.getUserId());
        assertEquals("Alice Example", result.getName());
        assertEquals("alice@example.com", result.getContactEmail());

        ArgumentCaptor<UserProfile> captor = ArgumentCaptor.forClass(UserProfile.class);
        verify(userProfileRepository).save(captor.capture());
        assertEquals("Alice Example", captor.getValue().getName());
        assertEquals("alice@example.com", captor.getValue().getContactEmail());
    }

    @Test
    @DisplayName("updateOwnProfile updates all editable fields")
    void updateOwnProfile_updatesFields() {
        User user = createUser(7L, "bob@example.com", "Bob", "Builder");
        UserProfile profile = new UserProfile();
        profile.setId(7L);
        profile.setUser(user);
        profile.setName("Alt");
        profile.setContactEmail("old@example.com");
        profile.setPictureUrl("old.png");
        profile.setDescription("alt");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("bob@example.com", "pw")
        );

        when(userRepository.findByEmail("bob@example.com")).thenReturn(Optional.of(user));
        when(userProfileRepository.findByUser_Id(7L)).thenReturn(Optional.of(profile));
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        dto.setName("Bob Builder");
        dto.setContactEmail("bob.profile@example.com");
        dto.setPictureUrl("https://example.com/bob.png");
        dto.setDescription("Neue Beschreibung");

        UserProfileResponseDTO result = service.updateOwnProfile(dto);

        assertEquals("Bob Builder", result.getName());
        assertEquals("bob.profile@example.com", result.getContactEmail());
        assertEquals("https://example.com/bob.png", result.getPictureUrl());
        assertEquals("Neue Beschreibung", result.getDescription());
    }

    @Test
    @DisplayName("searchProfiles uses only profile fields and maps summaries")
    void searchProfiles_returnsSummaries() {
        UserProfile profile = new UserProfile();
        profile.setId(9L);
        profile.setName("Charlie Example");
        profile.setContactEmail("charlie.profile@example.com");
        profile.setPictureUrl("https://example.com/charlie.png");

        when(userProfileRepository.searchByNameOrContactEmail("char")).thenReturn(List.of(profile));

        List<UserProfileSummaryDTO> result = service.searchProfiles("char");

        assertEquals(1, result.size());
        assertEquals(9L, result.get(0).getUserId());
        assertEquals("Charlie Example", result.get(0).getName());
        assertEquals("charlie.profile@example.com", result.get(0).getContactEmail());
        assertEquals("https://example.com/charlie.png", result.get(0).getPictureUrl());
    }

    @Test
    @DisplayName("getProfileByUserId returns public profile view")
    void getProfileByUserId_returnsProfile() {
        User user = createUser(10L, "dana@example.com", "Dana", "Example");

        UserProfile profile = new UserProfile();
        profile.setId(10L);
        profile.setUser(user);
        profile.setName("Dana Example");
        profile.setContactEmail("dana.profile@example.com");
        profile.setPictureUrl("https://example.com/dana.png");
        profile.setDescription("Öffentliches Profil");

        when(userProfileRepository.findByUser_Id(10L)).thenReturn(Optional.of(profile));

        UserProfileResponseDTO result = service.getProfileByUserId(10L);

        assertEquals(10L, result.getUserId());
        assertEquals("Dana Example", result.getName());
        assertEquals("dana.profile@example.com", result.getContactEmail());
        assertEquals("https://example.com/dana.png", result.getPictureUrl());
        assertEquals("Öffentliches Profil", result.getDescription());
    }

    private User createUser(Long id, String email, String firstName, String lastName) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        return user;
    }
}