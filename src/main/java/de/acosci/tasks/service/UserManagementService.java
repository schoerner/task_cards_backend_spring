package de.acosci.tasks.service;

import de.acosci.tasks.model.dto.UserManagementCreateDTO;
import de.acosci.tasks.model.dto.UserManagementUpdateDTO;
import de.acosci.tasks.model.dto.UserResponseDTO;

import java.util.List;

public interface UserManagementService {
    List<UserResponseDTO> getUsers();
    UserResponseDTO getUserById(Long id);
    UserResponseDTO createUser(UserManagementCreateDTO dto);
    UserResponseDTO updateUser(Long id, UserManagementUpdateDTO dto);
    void deleteUserById(Long id);
}