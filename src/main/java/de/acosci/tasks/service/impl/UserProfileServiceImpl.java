package de.acosci.tasks.service.impl;

import de.acosci.tasks.model.dto.UserProfileResponseDTO;
import de.acosci.tasks.model.dto.UserProfileSummaryDTO;
import de.acosci.tasks.model.dto.UserProfileUpdateDTO;
import de.acosci.tasks.model.entity.User;
import de.acosci.tasks.model.entity.UserProfile;
import de.acosci.tasks.model.mapper.UserMapper;
import de.acosci.tasks.repository.UserProfileRepository;
import de.acosci.tasks.repository.UserRepository;
import de.acosci.tasks.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserProfileServiceImpl implements UserProfileService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponseDTO getOwnProfile() {
        return UserMapper.toUserProfileResponseDTO(getOrCreateOwnProfileEntity());
    }

    @Override
    public UserProfileResponseDTO updateOwnProfile(UserProfileUpdateDTO dto) {
        UserProfile profile = getOrCreateOwnProfileEntity();

        profile.setName(dto.getName());
        profile.setContactEmail(dto.getContactEmail());
        profile.setPictureUrl(dto.getPictureUrl());
        profile.setDescription(dto.getDescription());

        return UserMapper.toUserProfileResponseDTO(userProfileRepository.save(profile));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserProfileSummaryDTO> searchProfiles(String query) {
        String normalizedQuery = query == null ? "" : query.trim();

        return userProfileRepository.searchByNameOrContactEmail(normalizedQuery).stream()
                .limit(25)
                .map(UserMapper::toUserProfileSummaryDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponseDTO getProfileByUserId(Long userId) {
        UserProfile profile = userProfileRepository.findByUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("User profile not found for userId=" + userId));

        return UserMapper.toUserProfileResponseDTO(profile);
    }

    private UserProfile getOrCreateOwnProfileEntity() {
        User currentUser = getCurrentUser();

        return userProfileRepository.findByUser_Id(currentUser.getId())
                .orElseGet(() -> {
                    UserProfile profile = new UserProfile();
                    profile.setUser(currentUser);
                    profile.setName(buildDefaultName(currentUser));
                    profile.setContactEmail(currentUser.getEmail());
                    return userProfileRepository.save(profile);
                });
    }

    private String buildDefaultName(User user) {
        String firstName = user.getFirstName() == null ? "" : user.getFirstName().trim();
        String lastName = user.getLastName() == null ? "" : user.getLastName().trim();
        String fullName = (firstName + " " + lastName).trim();

        if (!fullName.isBlank()) {
            return fullName;
        }

        return user.getEmail();
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + email));
    }
}