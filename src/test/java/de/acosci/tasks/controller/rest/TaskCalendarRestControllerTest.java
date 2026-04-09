package de.acosci.tasks.controller.rest;

import de.acosci.tasks.common.config.JwtAuthenticationFilter;
import de.acosci.tasks.model.dto.TaskCalendarEntryDTO;
import de.acosci.tasks.service.TaskCalendarService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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
        controllers = TaskCalendarRestController.class,
        excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@Import(TaskCalendarRestControllerTest.TestSecurityConfig.class)
class TaskCalendarRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskCalendarService taskCalendarService;

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
    void getMyCalendarTasks_returnsOk() throws Exception {
        TaskCalendarEntryDTO dto = new TaskCalendarEntryDTO();
        dto.setId(5L);
        dto.setTitle("Kalender-Task");
        dto.setProjectName("Projekt A");
        dto.setBoardColumnName("In Progress");
        dto.setActive(true);
        dto.setArchived(false);
        dto.setDueDate(OffsetDateTime.parse("2026-04-10T09:00:00+02:00"));

        when(taskCalendarService.getCalendarTasksForCurrentUser()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/calendar/tasks").with(user("user@test.de").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5))
                .andExpect(jsonPath("$[0].title").value("Kalender-Task"))
                .andExpect(jsonPath("$[0].projectName").value("Projekt A"))
                .andExpect(jsonPath("$[0].boardColumnName").value("In Progress"))
                .andExpect(jsonPath("$[0].active").value(true));
    }
}