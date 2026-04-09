package de.acosci.tasks.controller.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.acosci.tasks.common.config.JwtAuthenticationFilter;
import de.acosci.tasks.model.dto.TaskCalendarReminderDTO;
import de.acosci.tasks.model.dto.TaskCreateDTO;
import de.acosci.tasks.model.dto.TaskResponseDTO;
import de.acosci.tasks.model.dto.TimeRecordResponseDTO;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = TaskRestController.class,
        excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@Import(TaskRestControllerTest.TestSecurityConfig.class)
class TaskRestControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean(name = "projectSecurity")
    private de.acosci.tasks.security.ProjectSecurity projectSecurity;

    @MockitoBean(name = "taskSecurity")
    private de.acosci.tasks.security.TaskSecurity taskSecurity;

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
    void getTasksByProject_returnsOk() throws Exception {
        when(projectSecurity.isMemberByEmail(eq(1L), any())).thenReturn(true);
        when(taskService.getTasksByProject(1L)).thenReturn(List.of(sampleTask()));

        mockMvc.perform(get("/api/v1/projects/1/tasks").with(user("user@test.de").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5))
                .andExpect(jsonPath("$[0].boardColumnId").value(10));
    }

    @Test
    void createTask_returnsCreated() throws Exception {
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setProjectId(1L);
        dto.setTitle("Task");
        dto.setPriority(TaskPriority.MEDIUM);
        dto.setStartAt(OffsetDateTime.parse("2026-04-10T08:30:00+02:00"));
        dto.setDueDate(OffsetDateTime.parse("2026-04-10T10:00:00+02:00"));
        dto.setLocation("Büro 2.12");

        TaskCalendarReminderDTO reminder = new TaskCalendarReminderDTO();
        reminder.setMinutesBefore(15);
        reminder.setActionType("DISPLAY");
        reminder.setMessage("Bitte vorbereiten");

        Set<TaskCalendarReminderDTO> reminders = new LinkedHashSet<>();
        reminders.add(reminder);
        dto.setCalendarReminders(reminders);

        when(projectSecurity.canEditTasksByEmail(eq(1L), any())).thenReturn(true);
        when(taskService.createTask(any(TaskCreateDTO.class))).thenReturn(sampleTask());

        mockMvc.perform(post("/api/v1/tasks")
                        .with(user("user@test.de").roles("USER"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.startAt").value("2026-04-10T08:30:00+02:00"))
                .andExpect(jsonPath("$.location").value("Büro 2.12"))
                .andExpect(jsonPath("$.calendarReminders[0].minutesBefore").value(15))
                .andExpect(jsonPath("$.calendarReminders[0].actionType").value("DISPLAY"));
    }

    @Test
    void moveTask_returnsMovedTaskDto() throws Exception {
        when(taskSecurity.canEditTaskByEmail(eq(5L), any())).thenReturn(true);
        TaskResponseDTO moved = sampleTask();
        moved.setBoardColumnId(11L);
        when(taskService.moveTask(5L, 11L)).thenReturn(moved);

        mockMvc.perform(patch("/api/v1/tasks/5/move")
                        .param("boardColumnId", "11")
                        .with(user("user@test.de").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.boardColumnId").value(11));
    }

    @Test
    void archiveTask_returnsOk() throws Exception {
        when(taskSecurity.canEditTaskByEmail(eq(5L), any())).thenReturn(true);
        when(taskService.archiveTask(5L)).thenReturn(sampleTask());

        mockMvc.perform(patch("/api/v1/tasks/5/archive").with(user("user@test.de").roles("USER")))
                .andExpect(status().isOk());
    }

    @Test
    void getTimeRecords_returnsOk() throws Exception {
        TimeRecordResponseDTO record = new TimeRecordResponseDTO();
        record.setId(1L);
        record.setTaskId(5L);

        when(taskSecurity.canViewTaskByEmail(eq(5L), any())).thenReturn(true);
        when(taskService.getTimeRecords(5L)).thenReturn(List.of(record));

        mockMvc.perform(get("/api/v1/tasks/5/time-records").with(user("user@test.de").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].taskId").value(5));
    }

    @Test
    void isActive_returnsOk() throws Exception {
        when(taskSecurity.canViewTaskByEmail(eq(5L), any())).thenReturn(true);
        when(taskService.isActive(5L)).thenReturn(true);

        mockMvc.perform(get("/api/v1/tasks/5/time-tracking/active").with(user("user@test.de").roles("USER")))
                .andExpect(status().isOk());
    }

    @Test
    void startTimeTracking_returnsOk() throws Exception {
        when(taskSecurity.canEditTaskByEmail(eq(5L), any())).thenReturn(true);
        when(taskService.startTimeTracking(5L)).thenReturn(sampleTask());

        mockMvc.perform(post("/api/v1/tasks/5/time-tracking/start").with(user("user@test.de").roles("USER")))
                .andExpect(status().isOk());
    }

    @Test
    void stopTimeTracking_returnsOk() throws Exception {
        when(taskSecurity.canEditTaskByEmail(eq(5L), any())).thenReturn(true);
        when(taskService.stopTimeTracking(5L)).thenReturn(sampleTask());

        mockMvc.perform(post("/api/v1/tasks/5/time-tracking/stop").with(user("user@test.de").roles("USER")))
                .andExpect(status().isOk());
    }

    @Test
    void deleteTask_returnsNoContent() throws Exception {
        when(taskSecurity.canEditTaskByEmail(eq(5L), any())).thenReturn(true);

        mockMvc.perform(delete("/api/v1/tasks/5").with(user("user@test.de").roles("USER")))
                .andExpect(status().isNoContent());
    }

    private TaskResponseDTO sampleTask() {
        TaskResponseDTO dto = new TaskResponseDTO();
        dto.setId(5L);
        dto.setProjectId(1L);
        dto.setBoardColumnId(10L);
        dto.setTitle("Task");
        dto.setPriority(TaskPriority.MEDIUM);
        dto.setEstimatedMinutes(0);
        dto.setTrackedMinutes(0);
        dto.setStartAt(OffsetDateTime.parse("2026-04-10T08:30:00+02:00"));
        dto.setDueDate(OffsetDateTime.parse("2026-04-10T10:00:00+02:00"));
        dto.setLocation("Büro 2.12");

        TaskCalendarReminderDTO reminder = new TaskCalendarReminderDTO();
        reminder.setMinutesBefore(15);
        reminder.setActionType("DISPLAY");
        reminder.setMessage("Bitte vorbereiten");

        Set<TaskCalendarReminderDTO> reminders = new LinkedHashSet<>();
        reminders.add(reminder);
        dto.setCalendarReminders(reminders);

        return dto;
    }
}