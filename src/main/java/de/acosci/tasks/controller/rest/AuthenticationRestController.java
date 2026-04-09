package de.acosci.tasks.controller.rest;

import de.acosci.tasks.model.dto.*;
import de.acosci.tasks.model.entity.User;
import de.acosci.tasks.model.mapper.UserMapper;
import de.acosci.tasks.service.impl.AuthenticationService;
import de.acosci.tasks.service.impl.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// https://medium.com/@tericcabrel/implement-jwt-authentication-in-a-spring-boot-3-application-5839e4fd8fac

@RestController
@RequestMapping("api/v1/auth")
@Tag(name = "Authentication", description = "REST-API für Registrierung und Anmeldung")
public class AuthenticationRestController {
    private final JwtService jwtService;
    private final AuthenticationService authenticationService;

    public AuthenticationRestController(JwtService jwtService, AuthenticationService authenticationService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }

    @Operation(
            summary = "Benutzer registrieren",
            description = "Legt einen neuen Benutzer anhand der Registrierungsdaten an."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Benutzer erfolgreich registriert",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))
            ),
            @ApiResponse(responseCode = "404", description = "Registrierung fehlgeschlagen", content = @Content),
            @ApiResponse(responseCode = "409", description = "Benutzer existiert bereits oder Konflikt bei der Registrierung", content = @Content)
    })
    @PostMapping("/signup")
    public ResponseEntity<UserResponseDTO> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Registrierungsdaten des Benutzers",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RegisterUserDTO.class))
            )
            @RequestBody RegisterUserDTO registerUserDto) {
        try {
            User registeredUser = authenticationService.signup(registerUserDto);
            return ResponseEntity.ok(UserMapper.toUserResponseDTO(registeredUser));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    @Operation(
            summary = "Benutzer anmelden",
            description = "Authentifiziert einen Benutzer und liefert ein JWT-Token mit Ablaufzeit zurück."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Anmeldung erfolgreich",
                    content = @Content(schema = @Schema(implementation = LoginResponseDTO.class))
            ),
            @ApiResponse(responseCode = "401", description = "Ungültige Anmeldedaten", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> authenticate(@RequestBody LoginUserDTO loginUserDto) {
        LoginResponseDTO loginResponse = authenticationService.login(loginUserDto, jwtService);
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refresh(@RequestBody RefreshTokenRequestDTO requestDto) {
        LoginResponseDTO loginResponse = authenticationService.refresh(requestDto.refreshToken(), jwtService);
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody LogoutRequestDTO requestDto) {
        authenticationService.logout(requestDto.refreshToken());
        return ResponseEntity.noContent().build();
    }
}