package de.acosci.tasks.service;

import de.acosci.tasks.model.entity.Task;
import de.acosci.tasks.model.entity.TimeRecord;
import de.acosci.tasks.model.entity.User;
import de.acosci.tasks.repository.TaskRepository;
import de.acosci.tasks.repository.TimeRecordRepository;
import de.acosci.tasks.service.impl.TaskServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/*
IOC-Container
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = {TaskServiceImpl.class}) // Embedded webserver wird für Test nicht hochgefahren
class TaskServiceTest {

    @MockitoBean
    private TaskRepository taskRepository;
    @MockitoBean
    private TimeRecordRepository timeRecordRepository;

    @Autowired
    private TaskServiceImpl taskService;

    private final User mockUser = new User(1L, "test@test.org", new Date(), "Geheim01", "Geheim01", "John", "Doe", new ArrayList<Task>());
    private final Task mockTask1 = new Task(1L, "Test Task 1", "Description for Test Task 1", mockUser, new ArrayList<>(), false);
    private final Task mockTask2 = new Task(2L, "Test Task 2", "Description for Test Task 1", mockUser, new ArrayList<>(), false);

    @BeforeEach
    void setUp() {
        mockUser.getTasks().add(mockTask1);
        mockUser.getTasks().add(mockTask2);
    }

    @Test
    void getAllTasks() {
        // Arrange
        when(taskRepository.findAll()).thenReturn(Arrays.asList(mockTask1, mockTask2));

        // Act
        var allTasks = taskService.getAllTasks();

        // Assert
        assertNotNull(allTasks);
        assertEquals(2, allTasks.size());
    }

    @Test
    void getTaskById_butTaskNotFound() {
        Task nonExistent = new Task();

        //assertThrows(EntityNotFoundException.class, () -> taskService.isActive(nonExistent));
        assertThrows(EntityNotFoundException.class, () -> taskService.isActive(-1L));
    }

    @Test
    void getActiveTasks_noneActive() {
        // Arrange
        when(taskRepository.findAll()).thenReturn(Arrays.asList(mockTask1, mockTask2));

        // Act
        var activeTasks = taskService.getActiveTasks();

        // Assert
        assertNotNull(activeTasks);
        assertEquals(0, activeTasks.size());
    }

    @Test
    void isActive_noStartedTask_noneIsActive() {
        // Assert
        when(taskRepository.findById(1L)).thenReturn(Optional.of(mockTask1));

        // Act
        assertDoesNotThrow(() -> {taskService.isActive(mockTask1);});
        boolean isActive = taskService.isActive(mockTask1);
        TimeRecord activeTimeRecord = taskService.getActiveTimeRecord(mockTask1);

        // Assert
        assertFalse(isActive);
        assertNull(activeTimeRecord);
    }

    @Test
    void startTaskByTask_taskIsActive() {
        // Arrange
        when(taskRepository.findAll()).thenReturn(Arrays.asList(mockTask1, mockTask2));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(mockTask1));
        when(taskRepository.save(mockTask1)).thenReturn(mockTask1);

        // Act
        Task startedTask = taskService.startTask(mockTask1);
        var activeTasks = taskService.getActiveTasks();
        boolean isActive = taskService.isActive(mockTask1);
        TimeRecord activeTimeRecord = taskService.getActiveTimeRecord(mockTask1);

        // Assert
        assertNotNull(startedTask);
        assertTrue(startedTask.isActive());

        assertNotNull(activeTasks);
        assertTrue(activeTasks.contains(startedTask));
        assertEquals(1, activeTasks.size());

        assertTrue(isActive);
        assertNotNull(activeTimeRecord);
    }

    @Test
    void startTaskByTaskID_taskIsActive() {
        // Arrange
        when(taskRepository.findAll()).thenReturn(Arrays.asList(mockTask1, mockTask2));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(mockTask1));
        when(taskRepository.save(mockTask1)).thenReturn(mockTask1);

        // Act
        Task startedTask = taskService.startTask(1L);
        var activeTasks = taskService.getActiveTasks();
        boolean isActive = taskService.isActive(1L);
        TimeRecord activeTimeRecord = taskService.getActiveTimeRecord(1L);

        // Assert
        assertNotNull(startedTask);
        assertTrue(startedTask.isActive());

        assertNotNull(activeTasks);
        assertTrue(activeTasks.contains(startedTask));
        assertEquals(1, activeTasks.size());

        assertTrue(isActive);
        assertNotNull(activeTimeRecord);
    }

    @Test
    void stopTaskByTaskID_taskIsNotActive() {
        // Arrange
        when(taskRepository.findAll()).thenReturn(Arrays.asList(mockTask1, mockTask2));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(mockTask1));
        when(taskRepository.findById(2L)).thenReturn(Optional.of(mockTask2));
        when(taskRepository.save(mockTask1)).thenReturn(mockTask1);

        // Act
        Task startedTask = taskService.startTask(1L);
        Task stoppedTask = taskService.stopTask(1L);
        var activeTasks = taskService.getActiveTasks();
        boolean isActive = taskService.isActive(1L);
        TimeRecord activeTimeRecord = taskService.getActiveTimeRecord(1L);

        // Assert
        assertNotNull(stoppedTask);
        assertFalse(stoppedTask.isActive());
        assertNotNull(activeTasks);
        assertEquals(0, activeTasks.size());
        assertFalse(activeTasks.contains(stoppedTask));
        assertFalse(isActive);
        assertNull(activeTimeRecord);
    }

    @Test
    void stopTaskByTask_taskIsNotActive() {
        // Arrange
        when(taskRepository.findAll()).thenReturn(Arrays.asList(mockTask1, mockTask2));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(mockTask1));
        when(taskRepository.findById(2L)).thenReturn(Optional.of(mockTask2));
        when(taskRepository.save(mockTask1)).thenReturn(mockTask1);

        // Act
        Task startedTask = taskService.startTask(mockTask1);
        Task stoppedTask = taskService.stopTask(mockTask1);
        var activeTasks = taskService.getActiveTasks();
        boolean isActive = taskService.isActive(mockTask1);
        TimeRecord activeTimeRecord = taskService.getActiveTimeRecord(mockTask1);

        // Assert
        assertNotNull(stoppedTask);
        assertFalse(stoppedTask.isActive());
        assertNotNull(activeTasks);
        assertEquals(0, activeTasks.size());
        assertFalse(activeTasks.contains(stoppedTask));
        assertFalse(isActive);
        assertNull(activeTimeRecord);
    }

    @Test
    void getAllTasks_validID() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(mockTask1));

        // Act
        Task actualTask = taskService.getTaskByID(1L);

        // Assert
        assertNotNull(actualTask);
        assertEquals(mockTask1, actualTask);
    }

    @Test
    void getAllTasks_invalidID_throwsEntityNotFoundException() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(mockTask1));
        when(taskRepository.findById(2L)).thenReturn(Optional.of(mockTask2));

        // Act and Assert
        assertThrows(EntityNotFoundException.class, () -> taskService.getTaskByID(0L));
        assertDoesNotThrow(() -> taskService.getTaskByID(1L));
        assertDoesNotThrow(() -> taskService.getTaskByID(2L));
        assertThrows(EntityNotFoundException.class, () -> taskService.getTaskByID(3L));
    }

}