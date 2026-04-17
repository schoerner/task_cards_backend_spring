package de.acosci.tasks.controller.rest;

import de.acosci.tasks.common.config.JwtAuthenticationFilter;
import de.acosci.tasks.model.dto.TaskResponseDTO;
import de.acosci.tasks.model.enums.TaskPriority;
import de.acosci.tasks.service.TaskService;
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

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = TaskFocusRestController.class,
        excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@Import(TaskFocusRestControllerTest.TestSecurityConfig.class)
class TaskFocusRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

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
    void getFocusTasks_returnsOk() throws Exception {
        when(taskService.getFocusTasks(10)).thenReturn(List.of(sampleTask(5L, true), sampleTask(6L, false)));

        mockMvc.perform(get("/api/v1/tasks/focus")
                        .param("limit", "10")
                        .with(user("user@test.de").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5))
                .andExpect(jsonPath("$[0].favorite").value(true))
                .andExpect(jsonPath("$[1].id").value(6))
                .andExpect(jsonPath("$[1].favorite").value(false));
    }

    @Test
    void getFocusTasks_withoutLimit_usesDefault() throws Exception {
        when(taskService.getFocusTasks(10)).thenReturn(List.of(sampleTask(5L, true)));

        mockMvc.perform(get("/api/v1/tasks/focus")
                        .with(user("user@test.de").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5))
                .andExpect(jsonPath("$[0].favorite").value(true));
    }

    private TaskResponseDTO sampleTask(Long id, boolean favorite) {
        TaskResponseDTO dto = new TaskResponseDTO();
        dto.setId(id);
        dto.setProjectId(1L);
        dto.setBoardColumnId(10L);
        dto.setPosition(0);
        dto.setFavorite(favorite);
        dto.setTitle("Focus Task " + id);
        dto.setPriority(TaskPriority.HIGH);
        dto.setEstimatedMinutes(30);
        dto.setTrackedMinutes(15);
        dto.setStartAt(OffsetDateTime.parse("2026-04-10T08:30:00+02:00"));
        dto.setDueDate(OffsetDateTime.parse("2026-04-10T10:00:00+02:00"));
        dto.setActive(false);
        return dto;
    }
}