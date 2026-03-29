package de.acosci.tasks.service;

import de.acosci.tasks.model.entity.User;

import java.util.List;

public interface UserService {
    List<User> getUsers();
    User saveUser(User user);
    User getUserByID(Long id);
    void deleteUserByID(Long id);
}
