package de.acosci.tasks.service;

import de.acosci.tasks.model.dto.LoginUserDTO;
import de.acosci.tasks.model.dto.RegisterUserDTO;
import de.acosci.tasks.model.entity.User;
import de.acosci.tasks.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    public AuthenticationService(
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User signup(RegisterUserDTO input) {
        User user = new User();
        user.setEmail(input.email());
        user.setFirstName(input.firstName());
        user.setLastName(input.lastName());
        // todo password verification
        user.setPassword(passwordEncoder.encode(input.password()));
        return userRepository.save(user);
    }

    public User authenticate(LoginUserDTO input) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.email(),
                        input.password()
                )
        );

        return userRepository.findByEmail(input.email())
                .orElseThrow();
    }
}
