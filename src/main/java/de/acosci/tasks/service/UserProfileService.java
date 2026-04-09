package de.acosci.tasks.service;

import de.acosci.tasks.model.dto.UserProfileResponseDTO;
import de.acosci.tasks.model.dto.UserProfileSummaryDTO;
import de.acosci.tasks.model.dto.UserProfileUpdateDTO;

import java.util.List;

public interface UserProfileService {
    UserProfileResponseDTO getOwnProfile();
    UserProfileResponseDTO updateOwnProfile(UserProfileUpdateDTO dto);
    List<UserProfileSummaryDTO> searchProfiles(String query);
    UserProfileResponseDTO getProfileByUserId(Long userId);
}