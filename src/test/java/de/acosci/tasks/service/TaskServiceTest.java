package de.acosci.tasks.service;

import de.acosci.tasks.model.dto.TaskCreateDTO;
import de.acosci.tasks.model.dto.TaskResponseDTO;
import de.acosci.tasks.model.dto.TaskUpdateDTO;
import de.acosci.tasks.model.dto.TimeRecordResponseDTO;
import de.acosci.tasks.model.entity.BoardColumn;
import de.acosci.tasks.model.entity.Project;
import de.acosci.tasks.model.entity.Task;
import de.acosci.tasks.model.entity.TimeRecord;
import de.acosci.tasks.model.entity.User;
import de.acosci.tasks.model.enums.TaskPriority;
import de.acosci.tasks.repository.BoardColumnRepository;
import de.acosci.tasks.repository.ProjectRepository;
import de.acosci.tasks.repository.TaskLabelRepository;
import de.acosci.tasks.repository.TaskRepository;
import de.acosci.tasks.repository.TimeRecordRepository;
import de.acosci.tasks.repository.UserRepository;
import de.acosci.tasks.service.impl.TaskServiceImpl;
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
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = {TaskServiceImpl.class})
class TaskServiceTest {

    @MockitoBean
    private TaskRepository taskRepository;
    @MockitoBean
    private ProjectRepository projectRepository;
    @MockitoBean
    private BoardColumnRepository boardColumnRepository;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private TaskLabelRepository taskLabelRepository;
    @MockitoBean
    private TimeRecordRepository timeRecordRepository;

    @Autowired
    private TaskServiceImpl taskService;

    private User mockUser;
    private Project mockProject;
    private BoardColumn defaultColumn;
    private Task mockTask;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@test.org");
        mockUser.setPassword("Geheim01");
        mockUser.setFirstName("John");
        mockUser.setLastName("Doe");

        mockProject = new Project();
        mockProject.setId(100L);
        mockProject.setName("Project A");
        mockProject.setCreator(mockUser);

        defaultColumn = new BoardColumn();
        defaultColumn.setId(10L);
        defaultColumn.setProject(mockProject);
        defaultColumn.setName("Not assigned");
        defaultColumn.setPosition(0);

        mockTask = new Task();
        mockTask.setId(5L);
        mockTask.setProject(mockProject);
        mockTask.setBoardColumn(defaultColumn);
        mockTask.setCreator(mockUser);
        mockTask.setTitle("Old title");
        mockTask.setPriority(TaskPriority.MEDIUM);
        mockTask.setTrackedMinutes(0);
        mockTask.setEstimatedMinutes(0);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(
                new UsernamePasswordAuthenticationToken(mockUser.getEmail(), null, Collections.emptyList())
        );
        SecurityContextHolder.setContext(context);

        when(userRepository.findByEmail(mockUser.getEmail())).thenReturn(Optional.of(mockUser));
        when(projectRepository.findById(mockProject.getId())).thenReturn(Optional.of(mockProject));
        when(boardColumnRepository.findByProject_IdAndName(mockProject.getId(), "Not assigned"))
                .thenReturn(Optional.of(defaultColumn));
        when(taskLabelRepository.findAllById(any())).thenReturn(Collections.emptyList());
        when(userRepository.findAllById(any())).thenReturn(Collections.emptyList());
        when(timeRecordRepository.findAllByTask_IdOrderByTimeStartDesc(mockTask.getId())).thenReturn(Collections.emptyList());
        when(timeRecordRepository.findFirstByTask_IdAndTimeEndIsNullOrderByTimeStartDesc(mockTask.getId()))
                .thenReturn(Optional.empty());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createTask_usesDefaultColumnWhenNoColumnProvided() {
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setProjectId(mockProject.getId());
        dto.setTitle("New task");
        dto.setDescription("Desc");
        dto.setPriority(TaskPriority.HIGH);

        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(77L);
            return task;
        });

        TaskResponseDTO created = taskService.createTask(dto);

        assertEquals("New task", created.getTitle());
        assertEquals(defaultColumn.getId(), created.getBoardColumn().getId());
        assertEquals(TaskPriority.HIGH, created.getPriority());
        assertEquals(0, created.getTrackedMinutes());
    }

    @Test
    void updateTask_updatesFields_butNotTrackedMinutesDirectly() {
        TaskUpdateDTO dto = new TaskUpdateDTO();
        dto.setTitle("Updated");
        dto.setDescription("Updated desc");
        dto.setPriority(TaskPriority.LOW);
        dto.setEstimatedMinutes(45);

        when(taskRepository.findById(mockTask.getId())).thenReturn(Optional.of(mockTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponseDTO updated = taskService.updateTask(mockTask.getId(), dto);

        assertEquals("Updated", updated.getTitle());
        assertEquals(TaskPriority.LOW, updated.getPriority());
        assertEquals(0, updated.getTrackedMinutes());
        assertEquals(45, updated.getEstimatedMinutes());
    }

    @Test
    void getTaskById_missingTaskThrows() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> taskService.getTaskById(999L));
    }

    @Test
    void archiveTask_setsArchivedFlag() {
        when(taskRepository.findById(mockTask.getId())).thenReturn(Optional.of(mockTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponseDTO archived = taskService.archiveTask(mockTask.getId());
        assertTrue(archived.isArchived());
    }

    @Test
    void getTimeRecords_returnsRepositoryResult() {
        TimeRecord r1 = new TimeRecord();
        r1.setId(1L);
        r1.setTask(mockTask);

        TimeRecord r2 = new TimeRecord();
        r2.setId(2L);
        r2.setTask(mockTask);

        when(taskRepository.findById(mockTask.getId())).thenReturn(Optional.of(mockTask));
        when(timeRecordRepository.findAllByTask_IdOrderByTimeStartDesc(mockTask.getId()))
                .thenReturn(List.of(r1, r2));

        List<TimeRecordResponseDTO> result = taskService.getTimeRecords(mockTask.getId());

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(mockTask.getId(), result.get(0).getTaskId());
    }

    @Test
    void isActive_returnsTrue_whenOpenTimeRecordExists() {
        TimeRecord activeRecord = new TimeRecord();
        activeRecord.setId(7L);
        activeRecord.setTask(mockTask);
        activeRecord.setTimeStart(new Date());
        activeRecord.setTimeEnd(null);

        when(taskRepository.findById(mockTask.getId())).thenReturn(Optional.of(mockTask));
        when(timeRecordRepository.findFirstByTask_IdAndTimeEndIsNullOrderByTimeStartDesc(mockTask.getId()))
                .thenReturn(Optional.of(activeRecord));

        assertTrue(taskService.isActive(mockTask.getId()));
    }

    @Test
    void isActive_returnsFalse_whenNoOpenTimeRecordExists() {
        when(taskRepository.findById(mockTask.getId())).thenReturn(Optional.of(mockTask));
        when(timeRecordRepository.findFirstByTask_IdAndTimeEndIsNullOrderByTimeStartDesc(mockTask.getId()))
                .thenReturn(Optional.empty());

        assertFalse(taskService.isActive(mockTask.getId()));
    }

    @Test
    void startTimeTracking_createsNewTimeRecord() {
        when(taskRepository.findById(mockTask.getId())).thenReturn(Optional.of(mockTask));
        when(timeRecordRepository.findFirstByTask_IdAndTimeEndIsNullOrderByTimeStartDesc(mockTask.getId()))
                .thenReturn(Optional.empty());
        when(timeRecordRepository.save(any(TimeRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponseDTO result = taskService.startTimeTracking(mockTask.getId());

        assertEquals(mockTask.getId(), result.getId());
        assertTrue(result.isActive());
        verify(timeRecordRepository).save(any(TimeRecord.class));
        verify(taskRepository).save(mockTask);
    }

    @Test
    void startTimeTracking_throwsWhenAlreadyActive() {
        TimeRecord activeRecord = new TimeRecord();
        activeRecord.setId(99L);
        activeRecord.setTask(mockTask);
        activeRecord.setTimeStart(new Date());
        activeRecord.setTimeEnd(null);

        when(taskRepository.findById(mockTask.getId())).thenReturn(Optional.of(mockTask));
        when(timeRecordRepository.findFirstByTask_IdAndTimeEndIsNullOrderByTimeStartDesc(mockTask.getId()))
                .thenReturn(Optional.of(activeRecord));

        assertThrows(IllegalStateException.class, () -> taskService.startTimeTracking(mockTask.getId()));

        verify(timeRecordRepository, never()).save(any(TimeRecord.class));
    }

    @Test
    void stopTimeTracking_closesActiveRecord_andUpdatesTrackedMinutes() {
        TimeRecord activeRecord = new TimeRecord();
        activeRecord.setId(15L);
        activeRecord.setTask(mockTask);
        activeRecord.setTimeStart(new Date(System.currentTimeMillis() - 30L * 60L * 1000L));
        activeRecord.setTimeEnd(null);

        mockTask.getTimeRecords().add(activeRecord);

        when(taskRepository.findById(mockTask.getId())).thenReturn(Optional.of(mockTask));
        when(timeRecordRepository.findFirstByTask_IdAndTimeEndIsNullOrderByTimeStartDesc(mockTask.getId()))
                .thenReturn(Optional.of(activeRecord));
        when(timeRecordRepository.save(any(TimeRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponseDTO result = taskService.stopTimeTracking(mockTask.getId());

        assertFalse(result.isActive());
        assertTrue(result.getTrackedMinutes() >= 29);
        verify(timeRecordRepository).save(activeRecord);
    }
}
