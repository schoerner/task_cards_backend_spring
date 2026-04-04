package de.acosci.tasks.service;

import de.acosci.tasks.model.dto.UserManagementCreateDTO;
import de.acosci.tasks.model.dto.UserManagementUpdateDTO;
import de.acosci.tasks.model.entity.User;

import java.util.List;

public interface UserManagementService {
    List<User> getUsers();
    User getUserById(Long id);
    User createUser(UserManagementCreateDTO dto);
    User updateUser(Long id, UserManagementUpdateDTO dto);
    void deleteUserById(Long id);
}