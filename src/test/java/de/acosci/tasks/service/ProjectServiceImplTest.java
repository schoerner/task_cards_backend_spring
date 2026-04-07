package de.acosci.tasks.service;

import de.acosci.tasks.model.dto.ProjectCreateDTO;
import de.acosci.tasks.model.entity.Project;
import de.acosci.tasks.model.entity.User;
import de.acosci.tasks.model.enums.BoardColumnType;
import de.acosci.tasks.repository.ProjectMemberRepository;
import de.acosci.tasks.repository.ProjectRepository;
import de.acosci.tasks.repository.UserRepository;
import de.acosci.tasks.service.impl.ProjectServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = {ProjectServiceImpl.class})
class ProjectServiceImplTest {

    @MockitoBean
    private ProjectRepository projectRepository;

    @MockitoBean
    private ProjectMemberRepository projectMemberRepository;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private ProjectServiceImpl projectService;

    private User currentUser;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setEmail("owner@test.de");

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(currentUser.getEmail(), null, Collections.emptyList()));
        SecurityContextHolder.setContext(context);

        when(userRepository.findByEmail(currentUser.getEmail())).thenReturn(Optional.of(currentUser));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project project = invocation.getArgument(0);
            project.setId(99L);
            return project;
        });
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createProject_createsFourInitialBoardColumns() {
        ProjectCreateDTO dto = new ProjectCreateDTO();
        dto.setName("Projekt X");
        dto.setDescription("Desc");

        Project created = projectService.createProject(dto);

        assertNotNull(created);
        assertEquals(4, created.getBoardColumns().size());

        var columns = created.getBoardColumns().stream().toList();
        assertEquals("Not assigned", columns.get(0).getName());
        assertEquals(BoardColumnType.SYSTEM, columns.get(0).getType());
        assertFalse(columns.get(0).isDeletable());

        assertEquals("To Do", columns.get(1).getName());
        assertEquals("In Progress", columns.get(2).getName());
        assertEquals("Done", columns.get(3).getName());
        assertTrue(columns.stream().allMatch(column -> column.getProject() == created));
    }
}
