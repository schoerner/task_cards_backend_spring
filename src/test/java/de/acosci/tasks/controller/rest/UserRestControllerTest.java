package de.acosci.tasks.controller.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.acosci.tasks.common.config.JwtAuthenticationFilter;
import de.acosci.tasks.model.entity.User;
import de.acosci.tasks.service.impl.UserServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.ComponentScan.Filter;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = UserRestController.class,
        excludeFilters = @Filter(
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
    private UserServiceImpl userService;

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
    @DisplayName("GET /api/v1/users ist für ADMIN erlaubt")
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_asAdmin_returnsOk() throws Exception {
        when(userService.getUsers()).thenReturn(List.of(new User(), new User()));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/users ist für normalen USER verboten")
    @WithMockUser(roles = "USER")
    void getAllUsers_asUser_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isForbidden());
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
    @DisplayName("POST /api/v1/users ist für ADMIN erlaubt")
    @WithMockUser(roles = "ADMIN")
    void createUser_asAdmin_returnsCreated() throws Exception {
        User input = new User();
        User saved = new User();
        when(userService.saveUser(any(User.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/v1/users ist für normalen USER verboten")
    @WithMockUser(roles = "USER")
    void createUser_asUser_returnsForbidden() throws Exception {
        User input = new User();

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/v1/users ist anonym nicht erlaubt")
    void createUser_anonymous_returnsUnauthorized() throws Exception {
        User input = new User();

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /api/v1/users/{id} ist für den eigenen User erlaubt")
    @WithMockUser(roles = "USER")
    void updateUserById_asSelf_returnsOk() throws Exception {
        userSecurity.allowSelf = true;
        User input = new User();
        User saved = new User();
        when(userService.saveUser(any(User.class))).thenReturn(saved);

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
        User input = new User();

        mockMvc.perform(put("/api/v1/users/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isForbidden());
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
}
