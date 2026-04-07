package de.acosci.tasks.controller.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.acosci.tasks.common.config.JwtAuthenticationFilter;
import de.acosci.tasks.model.dto.UserProfileResponseDTO;
import de.acosci.tasks.model.dto.UserProfileUpdateDTO;
import de.acosci.tasks.service.UserProfileService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = UserProfileRestController.class,
        excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@Import(UserProfileRestControllerTest.TestSecurityConfig.class)
class UserProfileRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserProfileService userProfileService;

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
    void getOwnProfile_returnsOk() throws Exception {
        UserProfileResponseDTO response = new UserProfileResponseDTO();
        response.setUserId(1L);
        response.setName("Max Mustermann");
        response.setContactEmail("kontakt@test.de");

        when(userProfileService.getOwnProfile()).thenReturn(response);

        mockMvc.perform(get("/api/v1/users/me/profile").with(user("user@test.de").roles("USER")))
                .andExpect(status().isOk());
    }

    @Test
    void updateOwnProfile_returnsOk() throws Exception {
        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        dto.setName("Display Name");
        dto.setContactEmail("kontakt@test.de");

        UserProfileResponseDTO response = new UserProfileResponseDTO();
        response.setUserId(1L);
        response.setName("Display Name");
        response.setContactEmail("kontakt@test.de");

        when(userProfileService.updateOwnProfile(any(UserProfileUpdateDTO.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/users/me/profile")
                        .with(user("user@test.de").roles("USER"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }
}
