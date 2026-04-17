package de.acosci.tasks.service;

import de.acosci.tasks.model.dto.TaskCalendarReminderDTO;
import de.acosci.tasks.model.dto.TaskCreateDTO;
import de.acosci.tasks.model.dto.TaskResponseDTO;
import de.acosci.tasks.model.dto.TaskUpdateDTO;
import de.acosci.tasks.model.dto.TimeRecordResponseDTO;
import de.acosci.tasks.model.entity.*;
import de.acosci.tasks.model.enums.TaskPriority;
import de.acosci.tasks.repository.*;
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
import java.util.List;
import java.util.Optional;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
    private UserFavoriteTaskRepository userFavoriteTaskRepository;
    @MockitoBean
    private TimeRecordRepository timeRecordRepository;
    @MockitoBean
    private ProjectMemberRepository projectMemberRepository;

    @Autowired
    private TaskServiceImpl taskService;

    private User mockUser;
    private Project mockProject;
    private BoardColumn defaultColumn;
    private BoardColumn doneColumn;
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

        doneColumn = new BoardColumn();
        doneColumn.setId(11L);
        doneColumn.setProject(mockProject);
        doneColumn.setName("Done");
        doneColumn.setPosition(1);

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
        when(boardColumnRepository.findFirstByProjectIdAndDeletableFalse(mockProject.getId()))
                .thenReturn(Optional.of(defaultColumn));

        when(boardColumnRepository.findFirstByProjectIdOrderByPositionAsc(mockProject.getId()))
                .thenReturn(Optional.of(defaultColumn));
        when(boardColumnRepository.findById(defaultColumn.getId())).thenReturn(Optional.of(defaultColumn));
        when(boardColumnRepository.findById(doneColumn.getId())).thenReturn(Optional.of(doneColumn));
        when(taskLabelRepository.findAllById(any())).thenReturn(Collections.emptyList());
        when(userRepository.findAllById(any())).thenReturn(Collections.emptyList());
        when(timeRecordRepository.findAllByTask_IdOrderByTimeStartDesc(mockTask.getId())).thenReturn(Collections.emptyList());
        when(projectMemberRepository.existsByProject_IdAndUser_Id(any(), any())).thenReturn(false);
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        when(userFavoriteTaskRepository.existsByUser_IdAndTask_Id(anyLong(), anyLong())).thenReturn(false);
        when(userFavoriteTaskRepository.findAllByUser_Id(anyLong())).thenReturn(List.of());
        when(userFavoriteTaskRepository.findByUser_IdAndTask_Id(anyLong(), anyLong())).thenReturn(Optional.empty());

        when(timeRecordRepository.findFirstByTask_IdAndUser_IdAndTimeEndIsNullOrderByTimeStartDesc(
                mockTask.getId(), mockUser.getId()))
                .thenReturn(Optional.empty());

        when(timeRecordRepository.findFirstByUser_IdAndTimeEndIsNullOrderByTimeStartDesc(mockUser.getId()))
                .thenReturn(Optional.empty());

        when(userFavoriteTaskRepository.existsByUser_IdAndTask_Id(anyLong(), anyLong()))
                .thenReturn(false);
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

        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponseDTO created = taskService.createTask(dto);

        assertEquals("New task", created.getTitle());
        assertEquals(defaultColumn.getId(), created.getBoardColumnId());
        assertEquals(TaskPriority.HIGH, created.getPriority());
        assertEquals(0, created.getTrackedMinutes());
    }

    @Test
    void createTask_setsStartAtLocationAndReminders() {
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setProjectId(mockProject.getId());
        dto.setTitle("Calendar task");
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

        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponseDTO created = taskService.createTask(dto);

        assertEquals(OffsetDateTime.parse("2026-04-10T08:30:00+02:00"), created.getStartAt());
        assertEquals("Büro 2.12", created.getLocation());
        assertEquals(1, created.getCalendarReminders().size());

        TaskCalendarReminderDTO createdReminder = created.getCalendarReminders().iterator().next();
        assertEquals(15, createdReminder.getMinutesBefore());
        assertEquals("DISPLAY", createdReminder.getActionType());
        assertEquals("Bitte vorbereiten", createdReminder.getMessage());
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
    void updateTask_updatesStartAtLocationAndReplacesReminders() {
        TaskCalendarReminder oldReminder = new TaskCalendarReminder();
        oldReminder.setId(1L);
        oldReminder.setTask(mockTask);
        oldReminder.setMinutesBefore(60);
        oldReminder.setActionType("DISPLAY");
        oldReminder.setMessage("Alt");
        mockTask.getCalendarReminders().add(oldReminder);

        TaskUpdateDTO dto = new TaskUpdateDTO();
        dto.setTitle("Updated");
        dto.setStartAt(OffsetDateTime.parse("2026-04-11T09:00:00+02:00"));
        dto.setDueDate(OffsetDateTime.parse("2026-04-11T11:00:00+02:00"));
        dto.setLocation("Meetingraum A");

        TaskCalendarReminderDTO newReminder = new TaskCalendarReminderDTO();
        newReminder.setMinutesBefore(1440);
        newReminder.setActionType("DISPLAY");
        newReminder.setMessage("Einen Tag vorher");

        Set<TaskCalendarReminderDTO> reminders = new LinkedHashSet<>();
        reminders.add(newReminder);
        dto.setCalendarReminders(reminders);

        when(taskRepository.findById(mockTask.getId())).thenReturn(Optional.of(mockTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponseDTO updated = taskService.updateTask(mockTask.getId(), dto);

        assertEquals(OffsetDateTime.parse("2026-04-11T09:00:00+02:00"), updated.getStartAt());
        assertEquals("Meetingraum A", updated.getLocation());
        assertEquals(1, updated.getCalendarReminders().size());

        TaskCalendarReminderDTO updatedReminder = updated.getCalendarReminders().iterator().next();
        assertEquals(1440, updatedReminder.getMinutesBefore());
        assertEquals("DISPLAY", updatedReminder.getActionType());
        assertEquals("Einen Tag vorher", updatedReminder.getMessage());
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
        when(timeRecordRepository.findFirstByTask_IdAndUser_IdAndTimeEndIsNullOrderByTimeStartDesc(
                mockTask.getId(), mockUser.getId()))
                .thenReturn(Optional.of(activeRecord));

        assertTrue(taskService.isActive(mockTask.getId()));
    }

    @Test
    void isActive_returnsFalse_whenNoOpenTimeRecordExists() {
        when(taskRepository.findById(mockTask.getId())).thenReturn(Optional.of(mockTask));
        when(timeRecordRepository.findFirstByTask_IdAndUser_IdAndTimeEndIsNullOrderByTimeStartDesc(
                mockTask.getId(), mockUser.getId()))
                .thenReturn(Optional.empty());

        assertFalse(taskService.isActive(mockTask.getId()));
    }

    @Test
    void startTimeTracking_createsNewTimeRecord() {
        when(taskRepository.findById(mockTask.getId())).thenReturn(Optional.of(mockTask));
        when(timeRecordRepository.findFirstByUser_IdAndTimeEndIsNullOrderByTimeStartDesc(mockUser.getId()))
                .thenReturn(Optional.empty());

        TimeRecord savedRecord = new TimeRecord();
        savedRecord.setId(123L);
        savedRecord.setTask(mockTask);
        savedRecord.setUser(mockUser);
        savedRecord.setTimeStart(new Date());
        savedRecord.setTimeEnd(null);

        when(timeRecordRepository.findFirstByTask_IdAndUser_IdAndTimeEndIsNullOrderByTimeStartDesc(
                mockTask.getId(), mockUser.getId()))
                .thenReturn(Optional.empty(), Optional.of(savedRecord));

        when(timeRecordRepository.save(any(TimeRecord.class))).thenAnswer(invocation -> {
            TimeRecord saved = invocation.getArgument(0);
            if (saved.getUser() == null) {
                saved.setUser(mockUser);
            }
            if (saved.getTask() == null) {
                saved.setTask(mockTask);
            }
            saved.setId(123L);
            return saved;
        });

        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponseDTO result = taskService.startTimeTracking(mockTask.getId());

        assertEquals(mockTask.getId(), result.getId());
        assertTrue(result.isActive());
        verify(timeRecordRepository).save(any(TimeRecord.class));
        verify(taskRepository).save(mockTask);
    }

    @Test
    void startTimeTracking_sameTaskAlreadyActive_returnsTaskWithoutCreatingSecondRecord() {
        TimeRecord activeRecord = new TimeRecord();
        activeRecord.setId(99L);
        activeRecord.setTask(mockTask);
        activeRecord.setUser(mockUser);
        activeRecord.setTimeStart(new Date());
        activeRecord.setTimeEnd(null);

        mockTask.getTimeRecords().add(activeRecord);

        when(taskRepository.findById(mockTask.getId())).thenReturn(Optional.of(mockTask));
        when(timeRecordRepository.findFirstByUser_IdAndTimeEndIsNullOrderByTimeStartDesc(mockUser.getId()))
                .thenReturn(Optional.of(activeRecord));
        when(timeRecordRepository.findFirstByTask_IdAndUser_IdAndTimeEndIsNullOrderByTimeStartDesc(
                mockTask.getId(), mockUser.getId()))
                .thenReturn(Optional.of(activeRecord));

        TaskResponseDTO result = taskService.startTimeTracking(mockTask.getId());

        assertEquals(mockTask.getId(), result.getId());
        assertTrue(result.isActive());
        verify(timeRecordRepository, never()).save(any(TimeRecord.class));
    }

    @Test
    void stopTimeTracking_closesActiveRecord_andUpdatesTrackedMinutes() {
        TimeRecord activeRecord = new TimeRecord();
        activeRecord.setId(15L);
        activeRecord.setTask(mockTask);
        activeRecord.setUser(mockUser);
        activeRecord.setTimeStart(new Date(System.currentTimeMillis() - 30L * 60L * 1000L));
        activeRecord.setTimeEnd(null);

        mockTask.getTimeRecords().add(activeRecord);

        when(taskRepository.findById(mockTask.getId())).thenReturn(Optional.of(mockTask));

        when(timeRecordRepository.findFirstByTask_IdAndUser_IdAndTimeEndIsNullOrderByTimeStartDesc(
                mockTask.getId(), mockUser.getId()))
                .thenReturn(Optional.of(activeRecord), Optional.empty());

        when(timeRecordRepository.save(any(TimeRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponseDTO result = taskService.stopTimeTracking(mockTask.getId());

        assertFalse(result.isActive());
        assertTrue(result.getTrackedMinutes() >= 30);
    }

    @Test
    void createTask_setsAssignees_whenUsersAreProjectMembers() {
        User assignee = new User();
        assignee.setId(2L);
        assignee.setEmail("member@test.org");
        assignee.setFirstName("Jane");
        assignee.setLastName("Doe");

        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setProjectId(mockProject.getId());
        dto.setTitle("Task with assignee");
        dto.setPriority(TaskPriority.MEDIUM);
        dto.setAssigneeIds(Set.of(assignee.getId()));

        when(projectMemberRepository.existsByProject_IdAndUser_Id(mockProject.getId(), assignee.getId()))
                .thenReturn(true);
        when(userRepository.findById(assignee.getId())).thenReturn(Optional.of(assignee));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponseDTO created = taskService.createTask(dto);

        assertEquals(1, created.getAssignees().size());
        assertEquals(assignee.getId(), created.getAssignees().iterator().next().getId());
        verify(projectMemberRepository).existsByProject_IdAndUser_Id(mockProject.getId(), assignee.getId());
        verify(userRepository).findById(assignee.getId());
    }

    @Test
    void createTask_throws_whenAssigneeIsNotProjectMember() {
        Long foreignUserId = 999L;

        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setProjectId(mockProject.getId());
        dto.setTitle("Task with invalid assignee");
        dto.setPriority(TaskPriority.MEDIUM);
        dto.setAssigneeIds(Set.of(foreignUserId));

        when(projectMemberRepository.existsByProject_IdAndUser_Id(mockProject.getId(), foreignUserId))
                .thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> taskService.createTask(dto));

        verify(projectMemberRepository).existsByProject_IdAndUser_Id(mockProject.getId(), foreignUserId);
        verify(userRepository, never()).findById(foreignUserId);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void updateTask_throws_whenAssigneeIsNotProjectMember() {
        Long foreignUserId = 999L;

        TaskUpdateDTO dto = new TaskUpdateDTO();
        dto.setTitle("Updated");
        dto.setAssigneeIds(Set.of(foreignUserId));

        when(taskRepository.findById(mockTask.getId())).thenReturn(Optional.of(mockTask));
        when(projectMemberRepository.existsByProject_IdAndUser_Id(mockProject.getId(), foreignUserId))
                .thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> taskService.updateTask(mockTask.getId(), dto));

        verify(projectMemberRepository).existsByProject_IdAndUser_Id(mockProject.getId(), foreignUserId);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void startTimeTracking_stopsPreviouslyActiveTaskOfSameUser_andStartsNewTask() {
        Task otherTask = new Task();
        otherTask.setId(6L);
        otherTask.setProject(mockProject);
        otherTask.setBoardColumn(defaultColumn);
        otherTask.setCreator(mockUser);
        otherTask.setTitle("Other task");
        otherTask.setPriority(TaskPriority.HIGH);
        otherTask.setTrackedMinutes(0);
        otherTask.setEstimatedMinutes(0);

        TimeRecord previousActiveRecord = new TimeRecord();
        previousActiveRecord.setId(200L);
        previousActiveRecord.setTask(otherTask);
        previousActiveRecord.setUser(mockUser);
        previousActiveRecord.setTimeStart(new Date(System.currentTimeMillis() - 20L * 60L * 1000L));
        previousActiveRecord.setTimeEnd(null);

        otherTask.getTimeRecords().add(previousActiveRecord);

        when(taskRepository.findById(mockTask.getId())).thenReturn(Optional.of(mockTask));
        when(timeRecordRepository.findFirstByUser_IdAndTimeEndIsNullOrderByTimeStartDesc(mockUser.getId()))
                .thenReturn(Optional.of(previousActiveRecord));

        when(timeRecordRepository.findFirstByTask_IdAndUser_IdAndTimeEndIsNullOrderByTimeStartDesc(
                mockTask.getId(), mockUser.getId()))
                .thenReturn(Optional.empty(), Optional.of(new TimeRecord(123L, new Date(), null, mockTask, mockUser)));

        when(timeRecordRepository.save(any(TimeRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponseDTO result = taskService.startTimeTracking(mockTask.getId());

        assertEquals(mockTask.getId(), result.getId());
        assertTrue(result.isActive());
        assertTrue(previousActiveRecord.getTimeEnd() != null);

        verify(timeRecordRepository).save(argThat(record ->
                record.getTask() != null
                        && record.getTask().getId().equals(otherTask.getId())
                        && record.getUser() != null
                        && record.getUser().getId().equals(mockUser.getId())
                        && record.getTimeEnd() != null
        ));
        verify(timeRecordRepository).save(argThat(record ->
                record.getTask() != null
                        && record.getTask().getId().equals(mockTask.getId())
                        && record.getUser() != null
                        && record.getUser().getId().equals(mockUser.getId())
                        && record.getTimeEnd() == null
        ));
        verify(taskRepository).save(otherTask);
        verify(taskRepository).save(mockTask);
    }

    @Test
    void reorderTasksInColumn_updatesPositionsInSameColumn() {
        Task firstTask = new Task();
        firstTask.setId(101L);
        firstTask.setProject(mockProject);
        firstTask.setBoardColumn(defaultColumn);
        firstTask.setCreator(mockUser);
        firstTask.setTitle("First");
        firstTask.setPriority(TaskPriority.MEDIUM);
        firstTask.setPosition(0);

        Task secondTask = new Task();
        secondTask.setId(102L);
        secondTask.setProject(mockProject);
        secondTask.setBoardColumn(defaultColumn);
        secondTask.setCreator(mockUser);
        secondTask.setTitle("Second");
        secondTask.setPriority(TaskPriority.MEDIUM);
        secondTask.setPosition(1);

        mockTask.setBoardColumn(defaultColumn);
        mockTask.setPosition(2);

        when(taskRepository.findAllByProject_IdAndBoardColumn_IdAndArchivedFalseOrderByPositionAscIdAsc(
                mockProject.getId(), defaultColumn.getId()))
                .thenReturn(List.of(firstTask, secondTask, mockTask));

        when(taskRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        taskService.reorderTasksInColumn(mockProject.getId(), defaultColumn.getId(), List.of(102L, 5L, 101L));

        assertEquals(2, firstTask.getPosition());
        assertEquals(0, secondTask.getPosition());
        assertEquals(1, mockTask.getPosition());
        assertEquals(defaultColumn.getId(), mockTask.getBoardColumn().getId());

        verify(taskRepository).saveAll(argThat(tasks -> {
            int count = 0;
            for (Task ignored : tasks) {
                count++;
            }
            return count == 3;
        }));
    }

    @Test
    void moveTaskBetweenColumns_movesTaskAndReordersSourceAndTarget() {
        Task sourceSibling = new Task();
        sourceSibling.setId(101L);
        sourceSibling.setProject(mockProject);
        sourceSibling.setBoardColumn(defaultColumn);
        sourceSibling.setCreator(mockUser);
        sourceSibling.setTitle("Source sibling");
        sourceSibling.setPriority(TaskPriority.MEDIUM);
        sourceSibling.setPosition(1);

        Task targetExisting = new Task();
        targetExisting.setId(201L);
        targetExisting.setProject(mockProject);
        targetExisting.setBoardColumn(doneColumn);
        targetExisting.setCreator(mockUser);
        targetExisting.setTitle("Target existing");
        targetExisting.setPriority(TaskPriority.MEDIUM);
        targetExisting.setPosition(0);

        mockTask.setBoardColumn(defaultColumn);
        mockTask.setPosition(0);

        when(taskRepository.findAllByProject_IdAndBoardColumn_IdAndArchivedFalseOrderByPositionAscIdAsc(
                mockProject.getId(), defaultColumn.getId()))
                .thenReturn(List.of(mockTask, sourceSibling));

        when(taskRepository.findAllByProject_IdAndBoardColumn_IdAndArchivedFalseOrderByPositionAscIdAsc(
                mockProject.getId(), doneColumn.getId()))
                .thenReturn(List.of(targetExisting));

        when(taskRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        taskService.moveTaskBetweenColumns(
                mockProject.getId(),
                defaultColumn.getId(),
                doneColumn.getId(),
                List.of(101L),
                List.of(5L, 201L)
        );

        assertEquals(doneColumn.getId(), mockTask.getBoardColumn().getId());
        assertEquals(0, mockTask.getPosition());
        assertEquals(defaultColumn.getId(), sourceSibling.getBoardColumn().getId());
        assertEquals(0, sourceSibling.getPosition());
        assertEquals(doneColumn.getId(), targetExisting.getBoardColumn().getId());
        assertEquals(1, targetExisting.getPosition());

        verify(taskRepository, times(2)).saveAll(argThat(tasks -> {
            int count = 0;
            for (Task ignored : tasks) {
                count++;
            }
            return count >= 1;
        }));
        verify(taskRepository).save(mockTask);
    }

    @Test
    void moveTaskBetweenColumns_throwsWhenSourceTaskIdsDoNotMatchExpectedRemainingTasks() {
        Task sourceSibling = new Task();
        sourceSibling.setId(101L);
        sourceSibling.setProject(mockProject);
        sourceSibling.setBoardColumn(defaultColumn);
        sourceSibling.setCreator(mockUser);
        sourceSibling.setTitle("Source sibling");
        sourceSibling.setPriority(TaskPriority.MEDIUM);
        sourceSibling.setPosition(1);

        Task targetExisting = new Task();
        targetExisting.setId(201L);
        targetExisting.setProject(mockProject);
        targetExisting.setBoardColumn(doneColumn);
        targetExisting.setCreator(mockUser);
        targetExisting.setTitle("Target existing");
        targetExisting.setPriority(TaskPriority.MEDIUM);
        targetExisting.setPosition(0);

        mockTask.setBoardColumn(defaultColumn);
        mockTask.setPosition(0);

        when(taskRepository.findAllByProject_IdAndBoardColumn_IdAndArchivedFalseOrderByPositionAscIdAsc(
                mockProject.getId(), defaultColumn.getId()))
                .thenReturn(List.of(mockTask, sourceSibling));

        when(taskRepository.findAllByProject_IdAndBoardColumn_IdAndArchivedFalseOrderByPositionAscIdAsc(
                mockProject.getId(), doneColumn.getId()))
                .thenReturn(List.of(targetExisting));

        assertThrows(IllegalArgumentException.class, () ->
                taskService.moveTaskBetweenColumns(
                        mockProject.getId(),
                        defaultColumn.getId(),
                        doneColumn.getId(),
                        List.of(5L), // falsch: moved task darf hier nicht mehr enthalten sein
                        List.of(5L, 201L)
                )
        );
    }

    @Test
    void setFavorite_addsFavoriteWhenMissing() {
        when(taskRepository.findById(mockTask.getId())).thenReturn(Optional.of(mockTask));
        when(userFavoriteTaskRepository.findByUser_IdAndTask_Id(mockUser.getId(), mockTask.getId()))
                .thenReturn(Optional.empty());
        when(userFavoriteTaskRepository.save(any(UserFavoriteTask.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(userFavoriteTaskRepository.existsByUser_IdAndTask_Id(mockUser.getId(), mockTask.getId()))
                .thenReturn(false, true);

        TaskResponseDTO result = taskService.setFavorite(mockTask.getId(), true);

        assertTrue(result.isFavorite());
        verify(userFavoriteTaskRepository).save(argThat(favorite ->
                favorite.getUser().getId().equals(mockUser.getId())
                        && favorite.getTask().getId().equals(mockTask.getId())
        ));
    }

    @Test
    void setFavorite_removesFavoriteWhenPresent() {
        when(taskRepository.findById(mockTask.getId())).thenReturn(Optional.of(mockTask));

        UserFavoriteTask existingFavorite = new UserFavoriteTask();
        existingFavorite.setId(new UserFavoriteTaskId(mockUser.getId(), mockTask.getId()));
        existingFavorite.setUser(mockUser);
        existingFavorite.setTask(mockTask);

        when(userFavoriteTaskRepository.findByUser_IdAndTask_Id(mockUser.getId(), mockTask.getId()))
                .thenReturn(Optional.of(existingFavorite));
        when(userFavoriteTaskRepository.existsByUser_IdAndTask_Id(mockUser.getId(), mockTask.getId()))
                .thenReturn(false);

        TaskResponseDTO result = taskService.setFavorite(mockTask.getId(), false);

        assertFalse(result.isFavorite());
        verify(userFavoriteTaskRepository).delete(existingFavorite);
    }

    @Test
    void getFocusTasks_prioritizesFavoritesThenPriorityThenDate() {
        Task favoriteTask = new Task();
        favoriteTask.setId(11L);
        favoriteTask.setProject(mockProject);
        favoriteTask.setBoardColumn(defaultColumn);
        favoriteTask.setCreator(mockUser);
        favoriteTask.setTitle("Favorite");
        favoriteTask.setPriority(TaskPriority.LOW);
        favoriteTask.setDueDate(OffsetDateTime.parse("2026-04-20T10:00:00+02:00"));

        Task urgentTask = new Task();
        urgentTask.setId(12L);
        urgentTask.setProject(mockProject);
        urgentTask.setBoardColumn(defaultColumn);
        urgentTask.setCreator(mockUser);
        urgentTask.setTitle("Urgent");
        urgentTask.setPriority(TaskPriority.URGENT);
        urgentTask.setDueDate(OffsetDateTime.parse("2026-04-21T10:00:00+02:00"));

        Task datedTask = new Task();
        datedTask.setId(13L);
        datedTask.setProject(mockProject);
        datedTask.setBoardColumn(defaultColumn);
        datedTask.setCreator(mockUser);
        datedTask.setTitle("Soon");
        datedTask.setPriority(TaskPriority.HIGH);
        datedTask.setDueDate(OffsetDateTime.parse("2026-04-19T10:00:00+02:00"));

        UserFavoriteTask favorite = new UserFavoriteTask();
        favorite.setId(new UserFavoriteTaskId(mockUser.getId(), favoriteTask.getId()));
        favorite.setUser(mockUser);
        favorite.setTask(favoriteTask);

        when(taskRepository.findAllByAssignees_IdAndArchivedFalse(mockUser.getId()))
                .thenReturn(List.of(urgentTask, datedTask, favoriteTask));
        when(userFavoriteTaskRepository.findAllByUser_Id(mockUser.getId()))
                .thenReturn(List.of(favorite));

        when(userFavoriteTaskRepository.existsByUser_IdAndTask_Id(mockUser.getId(), favoriteTask.getId()))
                .thenReturn(true);
        when(userFavoriteTaskRepository.existsByUser_IdAndTask_Id(mockUser.getId(), urgentTask.getId()))
                .thenReturn(false);
        when(userFavoriteTaskRepository.existsByUser_IdAndTask_Id(mockUser.getId(), datedTask.getId()))
                .thenReturn(false);

        when(timeRecordRepository.findFirstByTask_IdAndUser_IdAndTimeEndIsNullOrderByTimeStartDesc(favoriteTask.getId(), mockUser.getId()))
                .thenReturn(Optional.empty());
        when(timeRecordRepository.findFirstByTask_IdAndUser_IdAndTimeEndIsNullOrderByTimeStartDesc(urgentTask.getId(), mockUser.getId()))
                .thenReturn(Optional.empty());
        when(timeRecordRepository.findFirstByTask_IdAndUser_IdAndTimeEndIsNullOrderByTimeStartDesc(datedTask.getId(), mockUser.getId()))
                .thenReturn(Optional.empty());

        List<TaskResponseDTO> result = taskService.getFocusTasks(10);

        assertEquals(3, result.size());
        assertEquals(11L, result.get(0).getId());
        assertEquals(12L, result.get(1).getId());
        assertEquals(13L, result.get(2).getId());
    }

    @Test
    void archiveTask_normalizesRemainingPositionsInColumn() {
        Task secondTask = new Task();
        secondTask.setId(6L);
        secondTask.setProject(mockProject);
        secondTask.setBoardColumn(defaultColumn);
        secondTask.setCreator(mockUser);
        secondTask.setTitle("Second");
        secondTask.setPriority(TaskPriority.MEDIUM);
        secondTask.setPosition(1);

        Task thirdTask = new Task();
        thirdTask.setId(7L);
        thirdTask.setProject(mockProject);
        thirdTask.setBoardColumn(defaultColumn);
        thirdTask.setCreator(mockUser);
        thirdTask.setTitle("Third");
        thirdTask.setPriority(TaskPriority.MEDIUM);
        thirdTask.setPosition(2);

        mockTask.setPosition(0);

        when(taskRepository.findById(mockTask.getId())).thenReturn(Optional.of(mockTask));
        when(taskRepository.findAllByProject_IdAndBoardColumn_IdAndArchivedFalseOrderByPositionAscIdAsc(
                mockProject.getId(), defaultColumn.getId()))
                .thenReturn(List.of(mockTask, secondTask, thirdTask));

        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(taskRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponseDTO archived = taskService.archiveTask(mockTask.getId());

        assertTrue(archived.isArchived());
        assertEquals(0, secondTask.getPosition());
        assertEquals(1, thirdTask.getPosition());
        verify(taskRepository).saveAll(argThat(tasks -> {
            int count = 0;
            for (Task ignored : tasks) {
                count++;
            }
            return count == 2;
        }));
    }

    @Test
    void restoreTask_placesTaskAtEndOfColumn() {
        mockTask.setArchived(true);
        mockTask.setPosition(0);

        Task existingTask = new Task();
        existingTask.setId(6L);
        existingTask.setProject(mockProject);
        existingTask.setBoardColumn(defaultColumn);
        existingTask.setCreator(mockUser);
        existingTask.setTitle("Existing");
        existingTask.setPriority(TaskPriority.MEDIUM);
        existingTask.setPosition(0);

        when(taskRepository.findById(mockTask.getId())).thenReturn(Optional.of(mockTask));
        when(taskRepository.findAllByProject_IdAndBoardColumn_IdAndArchivedFalseOrderByPositionAscIdAsc(
                mockProject.getId(), defaultColumn.getId()))
                .thenReturn(List.of(existingTask));

        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponseDTO restored = taskService.restoreTask(mockTask.getId());

        assertFalse(restored.isArchived());
        assertEquals(1, mockTask.getPosition());
    }

    @Test
    void deleteTask_normalizesRemainingPositionsInColumn() {
        Task secondTask = new Task();
        secondTask.setId(6L);
        secondTask.setProject(mockProject);
        secondTask.setBoardColumn(defaultColumn);
        secondTask.setCreator(mockUser);
        secondTask.setTitle("Second");
        secondTask.setPriority(TaskPriority.MEDIUM);
        secondTask.setPosition(1);

        Task thirdTask = new Task();
        thirdTask.setId(7L);
        thirdTask.setProject(mockProject);
        thirdTask.setBoardColumn(defaultColumn);
        thirdTask.setCreator(mockUser);
        thirdTask.setTitle("Third");
        thirdTask.setPriority(TaskPriority.MEDIUM);
        thirdTask.setPosition(2);

        mockTask.setPosition(0);

        when(taskRepository.findById(mockTask.getId())).thenReturn(Optional.of(mockTask));
        when(taskRepository.findAllByProject_IdAndBoardColumn_IdAndArchivedFalseOrderByPositionAscIdAsc(
                mockProject.getId(), defaultColumn.getId()))
                .thenReturn(List.of(secondTask, thirdTask));

        when(taskRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        taskService.deleteTask(mockTask.getId());

        verify(taskRepository).delete(mockTask);
        assertEquals(0, secondTask.getPosition());
        assertEquals(1, thirdTask.getPosition());
        verify(taskRepository).saveAll(argThat(tasks -> {
            int count = 0;
            for (Task ignored : tasks) {
                count++;
            }
            return count == 2;
        }));
    }
}