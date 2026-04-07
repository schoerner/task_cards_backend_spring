package de.acosci.tasks.service;

import de.acosci.tasks.model.dto.ChangePasswordDTO;
import de.acosci.tasks.model.dto.UserUpdateDTO;
import de.acosci.tasks.model.entity.User;

import java.util.List;

public interface UserService {
    List<User> getUsers();
    User saveUser(User user);
    User getUserByID(Long id);
    User updateUser(Long id, UserUpdateDTO dto);
    void deleteUserByID(Long id);
    void changePassword(Long id, ChangePasswordDTO dto);
}