package de.acosci.tasks.controller.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.acosci.tasks.common.config.JwtAuthenticationFilter;
import de.acosci.tasks.controller.rest.advice.RestExceptionHandler;
import de.acosci.tasks.model.dto.UserManagementCreateDTO;
import de.acosci.tasks.model.dto.UserManagementUpdateDTO;
import de.acosci.tasks.model.dto.UserResponseDTO;
import de.acosci.tasks.model.entity.Role;
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
import org.springframework.http.MediaType;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = UserManagementRestController.class,
        excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@Import({
        UserManagementRestControllerTest.TestSecurityConfig.class,
        RestExceptionHandler.class
})
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
    @DisplayName("GET /api/v1/user-management liefert für Admin 200 und Rollen im DTO")
    void getAllUsers_asAdmin_shouldReturnOk() throws Exception {
        UserResponseDTO user = new UserResponseDTO();
        user.setId(1L);
        user.setEmail("user@test.de");
        user.setRegistration(new Date());
        user.setFirstName("Max");
        user.setLastName("Mustermann");
        user.setRoles(Set.of("ROLE_USER", "ROLE_ADMIN"));

        when(userManagementService.getUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/v1/user-management")
                        .with(user("admin@test.de").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].email").value("user@test.de"))
                .andExpect(jsonPath("$[0].firstName").value("Max"))
                .andExpect(jsonPath("$[0].lastName").value("Mustermann"))
                .andExpect(jsonPath("$[0].roles.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/v1/user-management ist für normale User verboten")
    void getAllUsers_asUser_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/user-management")
                        .with(user("user@test.de").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/user-management/{id} liefert für Admin 200")
    void getUserById_asAdmin_shouldReturnOk() throws Exception {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(7L);
        dto.setEmail("edit@test.de");
        dto.setFirstName("Edit");
        dto.setLastName("User");
        dto.setRegistration(new Date());
        dto.setRoles(Set.of("ROLE_USER"));

        when(userManagementService.getUserById(7L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/user-management/7")
                        .with(user("admin@test.de").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.email").value("edit@test.de"))
                .andExpect(jsonPath("$.roles.length()").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/user-management/{id} liefert 404 bei unbekannter ID")
    void getUserById_whenMissing_shouldReturnNotFound() throws Exception {
        when(userManagementService.getUserById(999L))
                .thenThrow(new EntityNotFoundException("Der Benutzer wurde nicht gefunden."));

        mockMvc.perform(get("/api/v1/user-management/999")
                        .with(user("admin@test.de").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/user-management liefert für Admin 201")
    void createUser_asAdmin_shouldReturnCreated() throws Exception {
        UserManagementCreateDTO dto = new UserManagementCreateDTO(
                "new@test.de",
                "Secret123",
                "Ada",
                "Lovelace",
                Set.of(Role.RoleName.ROLE_USER)
        );

        UserResponseDTO createdUser = new UserResponseDTO();
        createdUser.setId(1L);
        createdUser.setEmail(dto.getEmail());
        createdUser.setFirstName(dto.getFirstName());
        createdUser.setLastName(dto.getLastName());
        createdUser.setRoles(Set.of("ROLE_USER"));

        when(userManagementService.createUser(any(UserManagementCreateDTO.class))).thenReturn(createdUser);

        mockMvc.perform(post("/api/v1/user-management")
                        .with(user("admin@test.de").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("new@test.de"))
                .andExpect(jsonPath("$.firstName").value("Ada"))
                .andExpect(jsonPath("$.lastName").value("Lovelace"))
                .andExpect(jsonPath("$.roles.length()").value(1));
    }

    @Test
    @DisplayName("POST /api/v1/user-management liefert 400 bei leerer Rollenliste")
    void createUser_whenInvalid_shouldReturnBadRequest() throws Exception {
        UserManagementCreateDTO dto = new UserManagementCreateDTO(
                "invalid@test.de",
                "Secret123",
                "Ada",
                "Lovelace",
                Set.of()
        );

        mockMvc.perform(post("/api/v1/user-management")
                        .with(user("admin@test.de").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Validierungsfehler in den übergebenen Daten."))
                .andExpect(jsonPath("$.fieldErrors.roles").value("must not be empty"));
    }

    @Test
    @DisplayName("PUT /api/v1/user-management/{id} liefert 200")
    void updateUser_asAdmin_shouldReturnOk() throws Exception {
        UserManagementUpdateDTO dto = new UserManagementUpdateDTO(
                "updated@test.de",
                "",
                "Grace",
                "Hopper",
                Set.of(Role.RoleName.ROLE_USER, Role.RoleName.ROLE_ADMIN)
        );

        UserResponseDTO updatedUser = new UserResponseDTO();
        updatedUser.setId(5L);
        updatedUser.setEmail(dto.getEmail());
        updatedUser.setFirstName(dto.getFirstName());
        updatedUser.setLastName(dto.getLastName());
        updatedUser.setRoles(Set.of("ROLE_USER", "ROLE_ADMIN"));

        when(userManagementService.updateUser(eq(5L), any(UserManagementUpdateDTO.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/v1/user-management/5")
                        .with(user("admin@test.de").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.email").value("updated@test.de"))
                .andExpect(jsonPath("$.roles.length()").value(2));
    }

    @Test
    @DisplayName("PUT /api/v1/user-management/{id} liefert 404 bei unbekannter ID")
    void updateUser_whenMissing_shouldReturnNotFound() throws Exception {
        UserManagementUpdateDTO dto = new UserManagementUpdateDTO(
                "missing@test.de",
                null,
                "Missing",
                "User",
                Set.of(Role.RoleName.ROLE_USER)
        );

        when(userManagementService.updateUser(eq(999L), any(UserManagementUpdateDTO.class)))
                .thenThrow(new EntityNotFoundException("missing"));

        mockMvc.perform(put("/api/v1/user-management/999")
                        .with(user("admin@test.de").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/v1/user-management/{id} liefert 400 bei leerer Rollenliste")
    void updateUser_whenInvalid_shouldReturnBadRequest() throws Exception {
        UserManagementUpdateDTO dto = new UserManagementUpdateDTO(
                "invalid@test.de",
                "",
                "Invalid",
                "User",
                Set.of()
        );

        mockMvc.perform(put("/api/v1/user-management/4")
                        .with(user("admin@test.de").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Validierungsfehler in den übergebenen Daten."))
                .andExpect(jsonPath("$.fieldErrors.roles").value("must not be empty"));
    }

    @Test
    @DisplayName("DELETE /api/v1/user-management/{id} liefert für Admin 200")
    void deleteUser_asAdmin_shouldReturnOk() throws Exception {
        doNothing().when(userManagementService).deleteUserById(1L);

        mockMvc.perform(delete("/api/v1/user-management/1")
                        .with(user("admin@test.de").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/user-management/{id} ist für normale User verboten")
    void deleteUser_asUser_shouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/user-management/1")
                        .with(user("user@test.de").roles("USER")))
                .andExpect(status().isForbidden());
    }
}