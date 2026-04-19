package de.acosci.tasks.controller.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.acosci.tasks.common.config.JwtAuthenticationFilter;
import de.acosci.tasks.model.dto.TaskPollResponseDTO;
import de.acosci.tasks.model.dto.TaskPollUpsertDTO;
import de.acosci.tasks.service.TaskPollMailService;
import de.acosci.tasks.service.TaskPollService;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TaskPollRestController.class,
        excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class))
@Import(TaskPollRestControllerTest.TestSecurityConfig.class)
class TaskPollRestControllerTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean TaskPollService taskPollService;
    @MockitoBean
    private TaskPollMailService taskPollMailService;
    @MockitoBean(name = "taskSecurity") de.acosci.tasks.security.TaskSecurity taskSecurity;

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {
        @Bean SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            return http.csrf(AbstractHttpConfigurer::disable)
                    .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(a -> a.anyRequest().authenticated())
                    .httpBasic(Customizer.withDefaults()).build();
        }
    }

    @Test
    void getPoll_returnsOk() throws Exception {
        when(taskSecurity.canViewTaskByEmail(eq(5L), any())).thenReturn(true);
        when(taskPollService.getPollByTask(5L)).thenReturn(sample());
        mockMvc.perform(get("/api/v1/tasks/5/poll").with(user("user@test.de").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").value(5))
                .andExpect(jsonPath("$.includedDates[0]").value("2026-05-05"));
    }


    @Test
    void deletePoll_returnsNoContent() throws Exception {
        when(taskSecurity.canEditTaskByEmail(eq(5L), any())).thenReturn(true);
        mockMvc.perform(delete("/api/v1/tasks/5/poll").with(user("user@test.de").roles("USER")))
                .andExpect(status().isNoContent());
    }

    @Test
    void savePoll_returnsOk() throws Exception {
        when(taskSecurity.canEditTaskByEmail(eq(5L), any())).thenReturn(true);
        TaskPollUpsertDTO dto = new TaskPollUpsertDTO();
        dto.setTitle("Review");
        dto.setStartDate(LocalDate.parse("2026-05-05"));
        dto.setEndDate(LocalDate.parse("2026-05-11"));
        dto.setDayStartTime(LocalTime.parse("08:00"));
        dto.setDayEndTime(LocalTime.parse("18:00"));
        dto.setSlotMinutes(30);
        dto.setIncludedDates(List.of(LocalDate.parse("2026-05-05")));

        when(taskPollService.savePoll(eq(5L), any(TaskPollUpsertDTO.class))).thenReturn(sample());
        mockMvc.perform(put("/api/v1/tasks/5/poll").with(user("user@test.de").roles("USER"))
                        .contentType("application/json").content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Review Poll"));
    }

    private TaskPollResponseDTO sample() {
        TaskPollResponseDTO dto = new TaskPollResponseDTO();
        dto.setId(1L);
        dto.setTaskId(5L);
        dto.setTitle("Review Poll");
        dto.setStartDate(LocalDate.parse("2026-05-05"));
        dto.setEndDate(LocalDate.parse("2026-05-11"));
        dto.setDayStartTime(LocalTime.parse("08:00"));
        dto.setDayEndTime(LocalTime.parse("18:00"));
        dto.setSlotMinutes(30);
        dto.setIncludedDates(List.of(LocalDate.parse("2026-05-05"), LocalDate.parse("2026-05-06")));
        return dto;
    }
}
