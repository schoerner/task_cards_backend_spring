package de.acosci.tasks.service;

import de.acosci.tasks.model.entity.User;
import de.acosci.tasks.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService implements IUserService {

    @Autowired // inject repository
    private UserRepository userRepository;

    @Override
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public User getUserByID(Long id) throws EntityNotFoundException {
        return userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("The user with id " + id + " was not found.") );

        //return userRepository.findById(id).orElseGet(User::new); //.orElseThrow(() -> new EntityNotFoundException("The user with id " + id + " was not found.") );

        /* So nicht mehr bei pot. null-Referenzen
        Optional<User> optional = userRepository.findById(id);
        User user = null;
        if(optional.isPresent()) {
            user = optional.get();
        } else {
            throw new EntityNotFoundException("The user with id " + id + " was not found.");
        }
        return user;
         */
    }

    @Override
    public void deleteUserByID(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public void deleteUser(User user) {
        userRepository.delete(user);
    }

/*
    public String generateToken(@NonNull User user)
    {

    }

    public Claims parseToke(@NonNull String token)
    {

    }

 */
}
