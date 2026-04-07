package de.acosci.tasks.service;

import de.acosci.tasks.model.dto.UserProfileResponseDTO;
import de.acosci.tasks.model.dto.UserProfileUpdateDTO;

public interface UserProfileService {
    UserProfileResponseDTO getOwnProfile();
    UserProfileResponseDTO updateOwnProfile(UserProfileUpdateDTO dto);
}