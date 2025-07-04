package de.acosci.tasks.controller.rest;

import de.acosci.tasks.model.dto.LoginResponseDTO;
import de.acosci.tasks.model.dto.LoginUserDTO;
import de.acosci.tasks.model.dto.RegisterUserDTO;
import de.acosci.tasks.model.entity.User;
import de.acosci.tasks.service.AuthenticationService;
import de.acosci.tasks.service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// https://medium.com/@tericcabrel/implement-jwt-authentication-in-a-spring-boot-3-application-5839e4fd8fac
@RequestMapping("/auth")
@RestController
public class AuthenticationRestController {
    private final JwtService jwtService;

    private final AuthenticationService authenticationService;

    public AuthenticationRestController(JwtService jwtService, AuthenticationService authenticationService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/signup")
    public ResponseEntity<User> register(@RequestBody RegisterUserDTO registerUserDto) {
        User registeredUser = authenticationService.signup(registerUserDto);
        return ResponseEntity.ok(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> authenticate(@RequestBody LoginUserDTO loginUserDto) {
        User authenticatedUser = authenticationService.authenticate(loginUserDto);

        String jwtToken = jwtService.generateToken(authenticatedUser);

        LoginResponseDTO loginResponse = new LoginResponseDTO(jwtToken, jwtService.getExpirationTime());

        return ResponseEntity.ok(loginResponse);
    }
}
