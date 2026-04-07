package de.acosci.tasks.service.impl;

import de.acosci.tasks.model.dto.ChangePasswordDTO;
import de.acosci.tasks.model.dto.UserUpdateDTO;
import de.acosci.tasks.model.entity.User;
import de.acosci.tasks.model.entity.UserProfile;
import de.acosci.tasks.repository.UserProfileRepository;
import de.acosci.tasks.repository.UserRepository;
import de.acosci.tasks.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Override
    public User saveUser(User user) {
        boolean isNewUser = user.getId() == null;
        User savedUser;

        if (isNewUser) {
            if (user.getPassword() != null && !user.getPassword().isBlank()) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }
            savedUser = userRepository.save(user);

            if (!userProfileRepository.existsById(savedUser.getId())) {
                UserProfile profile = new UserProfile();
                profile.setUser(savedUser);
                profile.setName(buildDefaultDisplayName(savedUser));
                profile.setContactEmail(savedUser.getEmail());
                profile.setPictureUrl(null);
                profile.setDescription(null);

                userProfileRepository.save(profile);
                savedUser.setProfile(profile);
            }
            return savedUser;
        }

        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("The user with id " + user.getId() + " was not found."));

        existingUser.setEmail(user.getEmail());
        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setRegistration(user.getRegistration());

        if (user.getRoles() != null) {
            existingUser.setRoles(user.getRoles());
        }

        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        return userRepository.save(existingUser);
    }

    @Override
    public User updateUser(Long id, UserUpdateDTO dto) {
        User existingUser = getUserByID(id);
        existingUser.setEmail(dto.getEmail());
        existingUser.setFirstName(dto.getFirstName());
        existingUser.setLastName(dto.getLastName());
        return userRepository.save(existingUser);
    }

    private String buildDefaultDisplayName(User user) {
        String firstName = user.getFirstName() != null ? user.getFirstName().trim() : "";
        String lastName = user.getLastName() != null ? user.getLastName().trim() : "";
        String fullName = (firstName + " " + lastName).trim();
        return fullName.isBlank() ? user.getEmail() : fullName;
    }

    @Override
    public User getUserByID(Long id) throws EntityNotFoundException {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("The user with id " + id + " was not found."));
    }

    @Override
    public void deleteUserByID(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public void changePassword(Long id, ChangePasswordDTO dto) {
        User user = getUserByID(id);

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Das aktuelle Passwort ist falsch.");
        }

        if (!dto.getNewPassword().equals(dto.getConfirmNewPassword())) {
            throw new IllegalArgumentException("Die neuen Passwörter stimmen nicht überein.");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }
}