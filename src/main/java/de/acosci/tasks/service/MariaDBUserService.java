package de.acosci.tasks.service;

import de.acosci.tasks.model.User;
import de.acosci.tasks.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MariaDBUserService implements UserService {

    @Autowired // inject repository
    private UserRepository userRepository;

    @Override
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Override
    public void saveUser(User user) {
        userRepository.save(user);
    }

    @Override
    public User getUserByID(Long id) {
        Optional<User> optional = userRepository.findById(id);
        User user = null;
        if(optional.isPresent()) {
            user = optional.get();
        } else {
            throw new RuntimeException("The user with id " + id + " was not found.");
        }
        return user;
    }

    @Override
    public void deleteUserByID(Long id) {
        userRepository.deleteById(id);
    }
}
