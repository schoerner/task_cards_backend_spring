package de.acosci.tasks.controller.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.acosci.tasks.common.config.JwtAuthenticationFilter;
import de.acosci.tasks.model.dto.UserManagementCreateDTO;
import de.acosci.tasks.model.dto.UserManagementUpdateDTO;
import de.acosci.tasks.model.entity.Role;
import de.acosci.tasks.model.entity.User;
import de.acosci.tasks.service.UserManagementService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = UserManagementRestController.class,
        excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@Import(UserManagementRestControllerTest.TestSecurityConfig.class)
class UserManagementRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserManagementService userManagementService;

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                    .httpBasic(Customizer.withDefaults())
                    .build();
        }
    }

    @Test
    @DisplayName("GET /api/v1/user-management liefert für Admin 200")
    void getAllUsers_asAdmin_shouldReturnOk() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@test.de");
        user.setRegistration(new Date());
        user.setFirstName("Max");
        user.setLastName("Mustermann");
        user.setRoles(Set.of(new Role(1L, Role.RoleName.ROLE_USER)));

        when(userManagementService.getUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/v1/user-management")
                        .with(user("admin@test.de").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/user-management ist für normale User verboten")
    void getAllUsers_asUser_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/user-management")
                        .with(user("user@test.de").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void createUser_asAdmin_shouldReturnCreated() throws Exception {
        UserManagementCreateDTO dto = new UserManagementCreateDTO(
                "new@test.de",
                "Secret123",
                "Ada",
                "Lovelace",
                Set.of(Role.RoleName.ROLE_USER)
        );

        User createdUser = new User();
        createdUser.setId(1L);
        createdUser.setEmail(dto.getEmail());
        createdUser.setFirstName(dto.getFirstName());
        createdUser.setLastName(dto.getLastName());
        createdUser.setRoles(Set.of(new Role(1L, Role.RoleName.ROLE_USER)));

        when(userManagementService.createUser(any(UserManagementCreateDTO.class))).thenReturn(createdUser);

        mockMvc.perform(post("/api/v1/user-management")
                        .with(user("admin@test.de").roles("ADMIN"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    void updateUser_whenMissing_shouldReturnNotFound() throws Exception {
        UserManagementUpdateDTO dto = new UserManagementUpdateDTO(
                "missing@test.de",
                null,
                "Missing",
                "User",
                Set.of(Role.RoleName.ROLE_USER)
        );

        when(userManagementService.updateUser(any(Long.class), any(UserManagementUpdateDTO.class)))
                .thenThrow(new EntityNotFoundException("missing"));

        mockMvc.perform(put("/api/v1/user-management/999")
                        .with(user("admin@test.de").roles("ADMIN"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_asAdmin_shouldReturnOk() throws Exception {
        doNothing().when(userManagementService).deleteUserById(1L);

        mockMvc.perform(delete("/api/v1/user-management/1")
                        .with(user("admin@test.de").roles("ADMIN")))
                .andExpect(status().isOk());
    }
}