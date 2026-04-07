package de.acosci.tasks.controller.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.acosci.tasks.common.config.JwtAuthenticationFilter;
import de.acosci.tasks.model.dto.ChangePasswordDTO;
import de.acosci.tasks.model.dto.UserUpdateDTO;
import de.acosci.tasks.model.entity.User;
import de.acosci.tasks.service.UserService;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = UserRestController.class,
        excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@Import(UserRestControllerTest.TestSecurityConfig.class)
class UserRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserSecurityStub userSecurity;

    @MockitoBean
    private UserService userService;

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

        @Bean(name = "userSecurity")
        UserSecurityStub userSecurity() {
            return new UserSecurityStub();
        }
    }

    static class UserSecurityStub {
        boolean allowSelf = false;

        public boolean isSelf(Long id) {
            return allowSelf;
        }

        public boolean isSelf(long id) {
            return allowSelf;
        }
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} ist für ADMIN erlaubt")
    @WithMockUser(roles = "ADMIN")
    void getUserById_asAdmin_returnsOk() throws Exception {
        when(userService.getUserByID(1L)).thenReturn(new User());

        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} ist für den eigenen User erlaubt")
    @WithMockUser(roles = "USER")
    void getUserById_asSelf_returnsOk() throws Exception {
        userSecurity.allowSelf = true;
        when(userService.getUserByID(1L)).thenReturn(new User());

        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} ist für fremden USER verboten")
    @WithMockUser(roles = "USER")
    void getUserById_asOtherUser_returnsForbidden() throws Exception {
        userSecurity.allowSelf = false;

        mockMvc.perform(get("/api/v1/users/2"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} liefert 404, wenn der User nicht existiert")
    @WithMockUser(roles = "ADMIN")
    void getUserById_notFound_returnsNotFound() throws Exception {
        when(userService.getUserByID(999L)).thenThrow(new EntityNotFoundException());

        mockMvc.perform(get("/api/v1/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} ist anonym nicht erlaubt")
    void getUserById_anonymous_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /api/v1/users/{id} ist für ADMIN erlaubt")
    @WithMockUser(roles = "ADMIN")
    void updateUserById_asAdmin_returnsOk() throws Exception {
        UserUpdateDTO input = new UserUpdateDTO();
        input.setEmail("test@test.de");
        input.setFirstName("Max");
        input.setLastName("Mustermann");

        User saved = new User();
        when(userService.updateUser(eq(1L), any(UserUpdateDTO.class))).thenReturn(saved);

        mockMvc.perform(put("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/v1/users/{id} ist für den eigenen User erlaubt")
    @WithMockUser(roles = "USER")
    void updateUserById_asSelf_returnsOk() throws Exception {
        userSecurity.allowSelf = true;

        UserUpdateDTO input = new UserUpdateDTO();
        input.setEmail("test@test.de");
        input.setFirstName("Max");
        input.setLastName("Mustermann");

        User saved = new User();
        when(userService.updateUser(eq(1L), any(UserUpdateDTO.class))).thenReturn(saved);

        mockMvc.perform(put("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/v1/users/{id} ist für fremden USER verboten")
    @WithMockUser(roles = "USER")
    void updateUserById_asOtherUser_returnsForbidden() throws Exception {
        userSecurity.allowSelf = false;

        UserUpdateDTO input = new UserUpdateDTO();
        input.setEmail("test@test.de");
        input.setFirstName("Max");
        input.setLastName("Mustermann");

        mockMvc.perform(put("/api/v1/users/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/v1/users/{id} liefert 404, wenn der User nicht existiert")
    @WithMockUser(roles = "ADMIN")
    void updateUserById_notFound_returnsNotFound() throws Exception {
        UserUpdateDTO input = new UserUpdateDTO();
        input.setEmail("test@test.de");
        input.setFirstName("Max");
        input.setLastName("Mustermann");

        when(userService.updateUser(eq(999L), any(UserUpdateDTO.class)))
                .thenThrow(new EntityNotFoundException());

        mockMvc.perform(put("/api/v1/users/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/v1/users/{id} ist anonym nicht erlaubt")
    void updateUserById_anonymous_returnsUnauthorized() throws Exception {
        UserUpdateDTO input = new UserUpdateDTO();
        input.setEmail("test@test.de");
        input.setFirstName("Max");
        input.setLastName("Mustermann");

        mockMvc.perform(put("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /api/v1/users/{id} ist für den eigenen User erlaubt")
    @WithMockUser(roles = "USER")
    void deleteUserById_asSelf_returnsOk() throws Exception {
        userSecurity.allowSelf = true;
        doNothing().when(userService).deleteUserByID(1L);

        mockMvc.perform(delete("/api/v1/users/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/users/{id} ist für fremden USER verboten")
    @WithMockUser(roles = "USER")
    void deleteUserById_asOtherUser_returnsForbidden() throws Exception {
        userSecurity.allowSelf = false;

        mockMvc.perform(delete("/api/v1/users/2"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/v1/users/{id} ist für ADMIN erlaubt")
    @WithMockUser(roles = "ADMIN")
    void deleteUserById_asAdmin_returnsOk() throws Exception {
        doNothing().when(userService).deleteUserByID(1L);

        mockMvc.perform(delete("/api/v1/users/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/users/{id} ist anonym nicht erlaubt")
    void deleteUserById_anonymous_returnsUnauthorized() throws Exception {
        mockMvc.perform(delete("/api/v1/users/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PATCH /api/v1/users/{id}/password ist für den eigenen User erlaubt")
    @WithMockUser(roles = "USER")
    void changePassword_asSelf_returnsOk() throws Exception {
        userSecurity.allowSelf = true;
        ChangePasswordDTO input = new ChangePasswordDTO();
        input.setCurrentPassword("oldSecret");
        input.setNewPassword("newSecret123");
        input.setConfirmNewPassword("newSecret123");

        doNothing().when(userService).changePassword(any(Long.class), any(ChangePasswordDTO.class));

        mockMvc.perform(patch("/api/v1/users/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /api/v1/users/{id}/password ist für ADMIN erlaubt")
    @WithMockUser(roles = "ADMIN")
    void changePassword_asAdmin_returnsOk() throws Exception {
        ChangePasswordDTO input = new ChangePasswordDTO();
        input.setCurrentPassword("oldSecret");
        input.setNewPassword("newSecret123");
        input.setConfirmNewPassword("newSecret123");

        doNothing().when(userService).changePassword(any(Long.class), any(ChangePasswordDTO.class));

        mockMvc.perform(patch("/api/v1/users/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /api/v1/users/{id}/password ist für fremden USER verboten")
    @WithMockUser(roles = "USER")
    void changePassword_asOtherUser_returnsForbidden() throws Exception {
        userSecurity.allowSelf = false;
        ChangePasswordDTO input = new ChangePasswordDTO();
        input.setCurrentPassword("oldSecret");
        input.setNewPassword("newSecret123");
        input.setConfirmNewPassword("newSecret123");

        mockMvc.perform(patch("/api/v1/users/2/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /api/v1/users/{id}/password ist anonym nicht erlaubt")
    void changePassword_anonymous_returnsUnauthorized() throws Exception {
        ChangePasswordDTO input = new ChangePasswordDTO();
        input.setCurrentPassword("oldSecret");
        input.setNewPassword("newSecret123");
        input.setConfirmNewPassword("newSecret123");

        mockMvc.perform(patch("/api/v1/users/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PATCH /api/v1/users/{id}/password liefert 400 bei ungültigen Passwortdaten")
    @WithMockUser(roles = "USER")
    void changePassword_invalidData_returnsBadRequest() throws Exception {
        userSecurity.allowSelf = true;
        ChangePasswordDTO input = new ChangePasswordDTO();
        input.setCurrentPassword("wrongOldSecret");
        input.setNewPassword("newSecret123");
        input.setConfirmNewPassword("differentSecret123");

        doThrow(new IllegalArgumentException("Die neuen Passwörter stimmen nicht überein."))
                .when(userService).changePassword(any(Long.class), any(ChangePasswordDTO.class));

        mockMvc.perform(patch("/api/v1/users/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/v1/users/{id}/password liefert 404, wenn der User nicht existiert")
    @WithMockUser(roles = "ADMIN")
    void changePassword_notFound_returnsNotFound() throws Exception {
        ChangePasswordDTO input = new ChangePasswordDTO();
        input.setCurrentPassword("oldSecret");
        input.setNewPassword("newSecret123");
        input.setConfirmNewPassword("newSecret123");

        doThrow(new EntityNotFoundException())
                .when(userService).changePassword(any(Long.class), any(ChangePasswordDTO.class));

        mockMvc.perform(patch("/api/v1/users/999/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isNotFound());
    }
}
