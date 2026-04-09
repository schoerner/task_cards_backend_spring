package de.acosci.tasks.controller.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.acosci.tasks.common.config.JwtAuthenticationFilter;
import de.acosci.tasks.model.dto.ProjectMemberUpdateDTO;
import de.acosci.tasks.model.dto.UserProfileSummaryDTO;
import de.acosci.tasks.model.entity.Project;
import de.acosci.tasks.model.entity.ProjectMember;
import de.acosci.tasks.model.entity.User;
import de.acosci.tasks.model.enums.ProjectRole;
import de.acosci.tasks.service.ProjectMembershipService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ProjectMemberRestController.class,
        excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@Import(ProjectMemberRestControllerTest.TestSecurityConfig.class)
class ProjectMemberRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProjectMembershipService projectMembershipService;

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
    @DisplayName("GET /api/v1/projects/{projectId}/members liefert 200 und flaches DTO")
    void getMembers_returnsOk() throws Exception {
        Project project = new Project();
        project.setId(2L);

        User user = new User();
        user.setId(1L);
        user.setEmail("admin@example.com");
        user.setFirstName("Admin");
        user.setLastName("User");

        ProjectMember member = new ProjectMember();
        member.setProject(project);
        member.setUser(user);
        member.setRole(ProjectRole.OWNER);
        member.setJoinedAt(OffsetDateTime.now());

        when(projectSecurity.isMemberByEmail(eq(2L), any())).thenReturn(true);
        when(projectMembershipService.getProjectMembers(2L)).thenReturn(List.of(member));

        mockMvc.perform(get("/api/v1/projects/2/members")
                        .with(user("admin@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].projectId").value(2))
                .andExpect(jsonPath("$[0].userId").value(1))
                .andExpect(jsonPath("$[0].email").value("admin@example.com"))
                .andExpect(jsonPath("$[0].role").value("OWNER"));
    }

    @Test
    void searchMemberCandidates_returnsOk() throws Exception {
        UserProfileSummaryDTO candidate = new UserProfileSummaryDTO();
        candidate.setUserId(5L);
        candidate.setName("Mia Musterfrau");
        candidate.setContactEmail("mia@example.org");

        when(projectSecurity.canManageMembersByEmail(eq(2L), any())).thenReturn(true);
        when(projectMembershipService.searchMemberCandidates(2L, "mia")).thenReturn(List.of(candidate));

        mockMvc.perform(get("/api/v1/projects/2/members/candidates")
                        .param("query", "mia")
                        .with(user("owner@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(5))
                .andExpect(jsonPath("$[0].name").value("Mia Musterfrau"));
    }

    @Test
    void addMember_returnsCreated() throws Exception {
        ProjectMemberUpdateDTO dto = new ProjectMemberUpdateDTO();
        dto.setUserId(2L);
        dto.setRole(ProjectRole.MEMBER);

        Project project = new Project();
        project.setId(2L);

        User user = new User();
        user.setId(2L);
        user.setEmail("member@example.com");
        user.setFirstName("Test");
        user.setLastName("Member");

        ProjectMember member = new ProjectMember();
        member.setProject(project);
        member.setUser(user);
        member.setRole(ProjectRole.MEMBER);

        when(projectSecurity.canManageMembersByEmail(eq(2L), any())).thenReturn(true);
        when(projectMembershipService.addMember(eq(2L), any(ProjectMemberUpdateDTO.class))).thenReturn(member);

        mockMvc.perform(post("/api/v1/projects/2/members")
                        .with(user("owner@example.com").roles("USER"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.projectId").value(2))
                .andExpect(jsonPath("$.userId").value(2))
                .andExpect(jsonPath("$.role").value("MEMBER"));
    }

    @Test
    void updateMemberRole_returnsOk() throws Exception {
        ProjectMemberUpdateDTO dto = new ProjectMemberUpdateDTO();
        dto.setUserId(2L);
        dto.setRole(ProjectRole.MAINTAINER);

        Project project = new Project();
        project.setId(2L);

        User user = new User();
        user.setId(2L);
        user.setEmail("member@example.com");

        ProjectMember member = new ProjectMember();
        member.setProject(project);
        member.setUser(user);
        member.setRole(ProjectRole.MAINTAINER);

        when(projectSecurity.canManageMembersByEmail(eq(2L), any())).thenReturn(true);
        when(projectMembershipService.updateMemberRole(eq(2L), eq(2L), any(ProjectMemberUpdateDTO.class))).thenReturn(member);

        mockMvc.perform(put("/api/v1/projects/2/members/2")
                        .with(user("owner@example.com").roles("USER"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(2))
                .andExpect(jsonPath("$.role").value("MAINTAINER"));
    }

    @Test
    void removeMember_returnsNoContent() throws Exception {
        when(projectSecurity.canManageMembersByEmail(eq(2L), any())).thenReturn(true);
        doNothing().when(projectMembershipService).removeMember(2L, 2L);

        mockMvc.perform(delete("/api/v1/projects/2/members/2")
                        .with(user("owner@example.com").roles("USER")))
                .andExpect(status().isNoContent());
    }
}
