package de.acosci.tasks.service.impl;

import de.acosci.tasks.model.dto.LoginResponseDTO;
import de.acosci.tasks.model.dto.LoginUserDTO;
import de.acosci.tasks.model.dto.RegisterUserDTO;
import de.acosci.tasks.model.entity.Role;
import de.acosci.tasks.model.entity.User;
import de.acosci.tasks.repository.RoleRepository;
import de.acosci.tasks.repository.UserRepository;
import de.acosci.tasks.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@RequiredArgsConstructor
@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    public User signup(RegisterUserDTO input) {
        User user = new User();
        user.setEmail(input.email());
        user.setFirstName(input.firstName());
        user.setLastName(input.lastName());
        user.setPassword(passwordEncoder.encode(input.password()));
        Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Default role not found"));
        user.setRoles(Set.of(userRole));
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

    public LoginResponseDTO login(LoginUserDTO input, JwtService jwtService) {
        User authenticatedUser = authenticate(input);

        String accessToken = jwtService.generateToken(authenticatedUser);
        var refreshToken = refreshTokenService.createRefreshToken(authenticatedUser);

        return new LoginResponseDTO(
                accessToken,
                jwtService.getExpirationTime(),
                refreshToken.getToken(),
                refreshToken.getExpiresAt().toEpochMilli() - System.currentTimeMillis()
        );
    }

    public LoginResponseDTO refresh(String refreshTokenValue, JwtService jwtService) {
        var existingRefreshToken = refreshTokenService.verifyUsableToken(refreshTokenValue);
        var rotatedRefreshToken = refreshTokenService.rotateRefreshToken(existingRefreshToken);

        User user = existingRefreshToken.getUser();
        String accessToken = jwtService.generateToken(user);

        return new LoginResponseDTO(
                accessToken,
                jwtService.getExpirationTime(),
                rotatedRefreshToken.getToken(),
                rotatedRefreshToken.getExpiresAt().toEpochMilli() - System.currentTimeMillis()
        );
    }

    public void logout(String refreshTokenValue) {
        if (refreshTokenValue != null && !refreshTokenValue.isBlank()) {
            refreshTokenService.revokeToken(refreshTokenValue);
        }
    }
}
