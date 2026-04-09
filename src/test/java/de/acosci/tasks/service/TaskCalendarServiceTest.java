package de.acosci.tasks.service;

import de.acosci.tasks.model.dto.TaskCalendarEntryDTO;
import de.acosci.tasks.model.entity.BoardColumn;
import de.acosci.tasks.model.entity.Project;
import de.acosci.tasks.model.entity.Task;
import de.acosci.tasks.model.entity.TimeRecord;
import de.acosci.tasks.model.entity.User;
import de.acosci.tasks.repository.TaskRepository;
import de.acosci.tasks.repository.UserRepository;
import de.acosci.tasks.service.impl.TaskCalendarServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = {TaskCalendarServiceImpl.class})
class TaskCalendarServiceTest {

    @MockitoBean
    private TaskRepository taskRepository;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private TaskCalendarServiceImpl taskCalendarService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@test.org");

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(
                new UsernamePasswordAuthenticationToken(mockUser.getEmail(), null, Collections.emptyList())
        );
        SecurityContextHolder.setContext(context);

        when(userRepository.findByEmail(mockUser.getEmail())).thenReturn(Optional.of(mockUser));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCalendarTasksForCurrentUser_mapsTaskDataCorrectly() {
        Project project = new Project();
        project.setId(100L);
        project.setName("Projekt A");

        BoardColumn column = new BoardColumn();
        column.setId(10L);
        column.setName("In Progress");
        column.setProject(project);

        Task task = new Task();
        task.setId(5L);
        task.setTitle("Task mit Termin");
        task.setProject(project);
        task.setBoardColumn(column);
        task.setDueDate(OffsetDateTime.parse("2026-04-10T09:00:00+02:00"));
        task.setArchived(true);

        TimeRecord activeRecord = new TimeRecord();
        activeRecord.setId(99L);
        activeRecord.setTask(task);
        activeRecord.setTimeStart(new Date());
        activeRecord.setTimeEnd(null);
        task.getTimeRecords().add(activeRecord);

        when(taskRepository.findCalendarTasksForUser(mockUser.getId())).thenReturn(List.of(task));

        List<TaskCalendarEntryDTO> result = taskCalendarService.getCalendarTasksForCurrentUser();

        assertEquals(1, result.size());
        assertEquals(5L, result.get(0).getId());
        assertEquals("Task mit Termin", result.get(0).getTitle());
        assertEquals("Projekt A", result.get(0).getProjectName());
        assertEquals("In Progress", result.get(0).getBoardColumnName());
        assertTrue(result.get(0).isArchived());
        assertTrue(result.get(0).isActive());
    }
}