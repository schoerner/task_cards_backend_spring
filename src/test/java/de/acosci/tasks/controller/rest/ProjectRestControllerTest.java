package de.acosci.tasks.controller.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.acosci.tasks.common.config.JwtAuthenticationFilter;
import de.acosci.tasks.model.dto.ProjectCreateDTO;
import de.acosci.tasks.model.dto.ProjectResponseDTO;
import de.acosci.tasks.model.dto.ProjectUpdateDTO;
import de.acosci.tasks.model.entity.Project;
import de.acosci.tasks.service.ProjectService;
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

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ProjectRestController.class,
        excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@Import(ProjectRestControllerTest.TestSecurityConfig.class)
class ProjectRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProjectService projectService;

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
    @DisplayName("GET /api/v1/projects/my liefert 200 und Projektliste")
    void getMyProjects_returnsOk() throws Exception {
        Project project = new Project();
        project.setId(1L);
        project.setName("Projekt A");
        project.setDescription("Beschreibung");
        project.setArchived(false);
        project.setCreatedAt(OffsetDateTime.now());
        project.setUpdatedAt(OffsetDateTime.now());

        when(projectService.getProjectsVisibleForCurrentUser()).thenReturn(List.of(project));

        mockMvc.perform(get("/api/v1/projects/my").with(user("user@test.de").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Projekt A"))
                .andExpect(jsonPath("$[0].description").value("Beschreibung"))
                .andExpect(jsonPath("$[0].archived").value(false));
    }

    @Test
    void getProject_returnsOk() throws Exception {
        Project project = new Project();
        project.setId(1L);
        project.setName("Projekt A");
        project.setDescription("Beschreibung");
        project.setArchived(false);

        when(projectSecurity.isMemberByEmail(eq(1L), any())).thenReturn(true);
        when(projectService.getVisibleProjectById(1L)).thenReturn(project);

        mockMvc.perform(get("/api/v1/projects/1").with(user("user@test.de").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Projekt A"));
    }

    @Test
    void createProject_returnsCreated() throws Exception {
        ProjectCreateDTO dto = new ProjectCreateDTO();
        dto.setName("Projekt A");
        dto.setDescription("Desc");

        Project createdProject = new Project();
        createdProject.setId(1L);
        createdProject.setName("Projekt A");
        createdProject.setDescription("Desc");
        createdProject.setArchived(false);

        when(projectService.createProject(any(ProjectCreateDTO.class))).thenReturn(createdProject);

        mockMvc.perform(post("/api/v1/projects")
                        .with(user("user@test.de").roles("USER"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Projekt A"));
    }

    @Test
    void updateProject_returnsOk() throws Exception {
        ProjectUpdateDTO dto = new ProjectUpdateDTO();
        dto.setName("Projekt B");
        dto.setDescription("Neu");
        dto.setArchived(false);

        Project updatedProject = new Project();
        updatedProject.setId(1L);
        updatedProject.setName("Projekt B");
        updatedProject.setDescription("Neu");
        updatedProject.setArchived(false);

        when(projectSecurity.canManageByEmail(eq(1L), any())).thenReturn(true);
        when(projectService.updateProject(eq(1L), any(ProjectUpdateDTO.class))).thenReturn(updatedProject);

        mockMvc.perform(put("/api/v1/projects/1")
                        .with(user("owner@test.de").roles("USER"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Projekt B"));
    }

    @Test
    void archiveProject_returnsOk() throws Exception {
        Project archivedProject = new Project();
        archivedProject.setId(1L);
        archivedProject.setName("Projekt A");
        archivedProject.setArchived(true);

        when(projectSecurity.canManageByEmail(eq(1L), any())).thenReturn(true);
        when(projectService.archiveProject(1L)).thenReturn(archivedProject);

        mockMvc.perform(patch("/api/v1/projects/1/archive")
                        .with(user("owner@test.de").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.archived").value(true));
    }

    @Test
    void deleteProject_returnsNoContent() throws Exception {
        doNothing().when(projectService).deleteProject(1L);
        when(projectSecurity.canManageByEmail(eq(1L), any())).thenReturn(true);

        mockMvc.perform(delete("/api/v1/projects/1")
                        .with(user("owner@test.de").roles("USER")))
                .andExpect(status().isNoContent());
    }
}