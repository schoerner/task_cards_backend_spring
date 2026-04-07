package de.acosci.tasks.controller.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.acosci.tasks.common.config.JwtAuthenticationFilter;
import de.acosci.tasks.model.dto.BoardColumnCreateDTO;
import de.acosci.tasks.model.dto.BoardColumnUpdateDTO;
import de.acosci.tasks.model.entity.BoardColumn;
import de.acosci.tasks.model.entity.Project;
import de.acosci.tasks.model.enums.BoardColumnType;
import de.acosci.tasks.service.BoardColumnService;
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

import java.util.List;

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
        controllers = BoardColumnRestController.class,
        excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@Import(BoardColumnRestControllerTest.TestSecurityConfig.class)
class BoardColumnRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BoardColumnService boardColumnService;

    @MockitoBean(name = "projectSecurity")
    private de.acosci.tasks.security.ProjectSecurity projectSecurity;

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
    @DisplayName("GET /api/v1/projects/{projectId}/board-columns liefert flaches DTO")
    void getBoardColumns_returnsOk() throws Exception {
        when(projectSecurity.isMemberByEmail(eq(2L), any())).thenReturn(true);
        when(boardColumnService.getBoardColumns(2L)).thenReturn(List.of(sampleColumn()));

        mockMvc.perform(get("/api/v1/projects/2/board-columns").with(user("user@test.de").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].projectId").value(2))
                .andExpect(jsonPath("$[0].name").value("Not assigned"))
                .andExpect(jsonPath("$[0].type").value("SYSTEM"))
                .andExpect(jsonPath("$[0].deletable").value(false));
    }

    @Test
    void createColumn_returnsCreated() throws Exception {
        BoardColumnCreateDTO dto = new BoardColumnCreateDTO();
        dto.setName("Testing");
        dto.setPosition(4);

        when(projectSecurity.canManageBoardByEmail(eq(2L), any())).thenReturn(true);
        when(boardColumnService.createColumn(eq(2L), any(BoardColumnCreateDTO.class))).thenReturn(customColumn());

        mockMvc.perform(post("/api/v1/projects/2/board-columns")
                        .with(user("owner@test.de").roles("USER"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(11))
                .andExpect(jsonPath("$.projectId").value(2))
                .andExpect(jsonPath("$.name").value("Testing"));
    }

    @Test
    void updateColumn_returnsOk() throws Exception {
        BoardColumnUpdateDTO dto = new BoardColumnUpdateDTO();
        dto.setName("Review");
        dto.setPosition(5);

        BoardColumn updated = customColumn();
        updated.setName("Review");
        updated.setPosition(5);

        when(projectSecurity.canManageBoardByEmail(eq(2L), any())).thenReturn(true);
        when(boardColumnService.updateColumn(eq(2L), eq(11L), any(BoardColumnUpdateDTO.class))).thenReturn(updated);

        mockMvc.perform(put("/api/v1/projects/2/board-columns/11")
                        .with(user("owner@test.de").roles("USER"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Review"))
                .andExpect(jsonPath("$.position").value(5));
    }

    @Test
    void deleteColumn_returnsNoContent() throws Exception {
        when(projectSecurity.canManageBoardByEmail(eq(2L), any())).thenReturn(true);
        doNothing().when(boardColumnService).deleteColumn(2L, 11L, 10L);

        mockMvc.perform(delete("/api/v1/projects/2/board-columns/11")
                        .param("fallbackColumnId", "10")
                        .with(user("owner@test.de").roles("USER")))
                .andExpect(status().isNoContent());
    }

    private BoardColumn sampleColumn() {
        Project project = new Project();
        project.setId(2L);

        BoardColumn column = new BoardColumn();
        column.setId(10L);
        column.setProject(project);
        column.setName("Not assigned");
        column.setPosition(0);
        column.setType(BoardColumnType.SYSTEM);
        column.setDeletable(false);
        return column;
    }

    private BoardColumn customColumn() {
        Project project = new Project();
        project.setId(2L);

        BoardColumn column = new BoardColumn();
        column.setId(11L);
        column.setProject(project);
        column.setName("Testing");
        column.setPosition(4);
        column.setType(BoardColumnType.CUSTOM);
        column.setDeletable(true);
        return column;
    }
}
