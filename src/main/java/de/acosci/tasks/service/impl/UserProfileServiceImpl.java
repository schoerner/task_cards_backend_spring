package de.acosci.tasks.service.impl;

import de.acosci.tasks.model.dto.UserProfileResponseDTO;
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

    private UserProfile getOrCreateOwnProfileEntity() {
        User currentUser = getCurrentUser();

        return userProfileRepository.findByUser_Id(currentUser.getId())
                .orElseGet(() -> {
                    UserProfile profile = new UserProfile();
                    profile.setUser(currentUser);
                    profile.setName(buildDefaultDisplayName(currentUser));
                    profile.setContactEmail(currentUser.getEmail());
                    return userProfileRepository.save(profile);
                });
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + email));
    }

    private String buildDefaultDisplayName(User user) {
        String firstName = user.getFirstName() != null ? user.getFirstName().trim() : "";
        String lastName = user.getLastName() != null ? user.getLastName().trim() : "";
        String fullName = (firstName + " " + lastName).trim();
        return fullName.isBlank() ? user.getEmail() : fullName;
    }
}