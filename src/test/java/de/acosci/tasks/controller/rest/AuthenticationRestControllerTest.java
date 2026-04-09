package de.acosci.tasks.controller.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.acosci.tasks.common.config.JwtAuthenticationFilter;
import de.acosci.tasks.exceptions.GlobalExceptionHandler;
import de.acosci.tasks.model.dto.LoginResponseDTO;
import de.acosci.tasks.model.dto.LoginUserDTO;
import de.acosci.tasks.model.dto.LogoutRequestDTO;
import de.acosci.tasks.model.dto.RefreshTokenRequestDTO;
import de.acosci.tasks.service.impl.AuthenticationService;
import de.acosci.tasks.service.impl.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AuthenticationRestController.class,
        excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@Import({
        AuthenticationRestControllerTest.TestSecurityConfig.class,
        GlobalExceptionHandler.class
})
class AuthenticationRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private JwtService jwtService;

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {

        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .httpBasic(Customizer.withDefaults())
                    .build();
        }
    }

    @Test
    @DisplayName("POST /api/v1/auth/login liefert 200 und Access-/Refresh-Token")
    @WithAnonymousUser
    void login_shouldReturnOkWithTokens() throws Exception {
        LoginUserDTO request = new LoginUserDTO("max@test.de", "Secret123");

        LoginResponseDTO response = new LoginResponseDTO(
                "access-token-123",
                900000L,
                "refresh-token-456",
                604800000L
        );

        when(authenticationService.login(any(LoginUserDTO.class), eq(jwtService)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("access-token-123"))
                .andExpect(jsonPath("$.expiresIn").value(900000))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token-456"))
                .andExpect(jsonPath("$.refreshExpiresIn").value(604800000));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login liefert 401 bei ungültigen Zugangsdaten")
    @WithAnonymousUser
    void login_withBadCredentials_shouldReturnUnauthorized() throws Exception {
        LoginUserDTO request = new LoginUserDTO("max@test.de", "wrong");

        when(authenticationService.login(any(LoginUserDTO.class), eq(jwtService)))
                .thenThrow(new org.springframework.security.authentication.BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh liefert 200 und neue Tokens")
    @WithAnonymousUser
    void refresh_shouldReturnOkWithNewTokens() throws Exception {
        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO("refresh-token-old");

        LoginResponseDTO response = new LoginResponseDTO(
                "access-token-new",
                900000L,
                "refresh-token-new",
                604800000L
        );

        when(authenticationService.refresh(eq("refresh-token-old"), eq(jwtService)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("access-token-new"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token-new"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh liefert 401 bei abgelaufenem Refresh-Token")
    @WithAnonymousUser
    void refresh_withExpiredToken_shouldReturnUnauthorized() throws Exception {
        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO("refresh-token-expired");

        when(authenticationService.refresh(eq("refresh-token-expired"), eq(jwtService)))
                .thenThrow(new ExpiredJwtException(null, null, "Refresh token expired"));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh liefert 401 bei manipuliertem Token")
    @WithAnonymousUser
    void refresh_withInvalidSignature_shouldReturnUnauthorized() throws Exception {
        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO("refresh-token-invalid");

        when(authenticationService.refresh(eq("refresh-token-invalid"), eq(jwtService)))
                .thenThrow(new SignatureException("Invalid signature"));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    @DisplayName("POST /api/v1/auth/logout liefert 204")
    @WithAnonymousUser
    void logout_shouldReturnNoContent() throws Exception {
        LogoutRequestDTO request = new LogoutRequestDTO("refresh-token-456");

        doNothing().when(authenticationService).logout("refresh-token-456");

        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }
}