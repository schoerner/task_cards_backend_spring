package de.acosci.tasks.service;

import de.acosci.tasks.model.User;

import java.util.List;

public interface UserService {
    List<User> getUsers();
    void saveUser(User user);
    User getUserByID(Long id);
    void deleteUserByID(Long id);
}
