package de.acosci.tasks.model.mapper;

import de.acosci.tasks.model.dto.UserProfileResponseDTO;
import de.acosci.tasks.model.dto.UserResponseDTO;
import de.acosci.tasks.model.entity.Role;
import de.acosci.tasks.model.entity.User;
import de.acosci.tasks.model.entity.UserProfile;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/** Damit Controller und Service sauber bleiben. */
public final class UserMapper {

    private UserMapper() {
    }

    public static UserResponseDTO toUserResponseDTO(User user) {
        if (user == null) {
            return null;
        }

        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setRegistration(user.getRegistration());

        Set<String> roles = user.getRoles() == null
                ? Collections.emptySet()
                : user.getRoles().stream()
                  .map(Role::getName)
                  .map(Enum::name)
                  .collect(Collectors.toSet());

        dto.setRoles(roles);
        dto.setProfile(toUserProfileResponseDTO(user.getProfile()));
        return dto;
    }

    public static UserProfileResponseDTO toUserProfileResponseDTO(UserProfile profile) {
        if (profile == null) {
            return null;
        }

        UserProfileResponseDTO dto = new UserProfileResponseDTO();
        dto.setUserId(profile.getId());
        dto.setName(profile.getName());
        dto.setContactEmail(profile.getContactEmail());
        dto.setPictureUrl(profile.getPictureUrl());
        dto.setDescription(profile.getDescription());
        return dto;
    }
}