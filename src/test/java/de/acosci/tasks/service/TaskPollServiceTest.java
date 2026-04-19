package de.acosci.tasks.service;

import de.acosci.tasks.model.dto.TaskPollUpsertDTO;
import de.acosci.tasks.model.entity.Task;
import de.acosci.tasks.model.entity.TaskPoll;
import de.acosci.tasks.model.entity.User;
import de.acosci.tasks.repository.*;
import de.acosci.tasks.service.impl.TaskPollServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = {TaskPollServiceImpl.class})
class TaskPollServiceTest {
    @MockitoBean TaskPollRepository taskPollRepository;
    @MockitoBean TaskRepository taskRepository;
    @MockitoBean UserRepository userRepository;
    @MockitoBean TaskPollParticipantRepository taskPollParticipantRepository;
    @MockitoBean TaskPollAvailabilityRepository taskPollAvailabilityRepository;

    @Autowired TaskPollServiceImpl taskPollService;

    private User user;
    private Task task;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("user@test.de");
        user.setFirstName("Max");
        user.setLastName("Muster");

        task = new Task();
        task.setId(5L);
        task.setTitle("Task");

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user.getEmail(), null, List.of()));
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(taskRepository.findById(5L)).thenReturn(Optional.of(task));
        when(taskPollRepository.findByTask_Id(5L)).thenReturn(Optional.empty());
        AtomicReference<TaskPoll> savedPollRef = new AtomicReference<>();

        when(taskPollRepository.save(any(TaskPoll.class))).thenAnswer(invocation -> {
            TaskPoll saved = invocation.getArgument(0);
            if (saved.getId() == null) {
                saved.setId(1L);
            }
            savedPollRef.set(saved);
            return saved;
        });

        when(taskPollRepository.findById(1L)).thenAnswer(invocation ->
                java.util.Optional.ofNullable(savedPollRef.get()));
        when(taskPollParticipantRepository.findByPoll_IdAndUser_Id(any(), any())).thenReturn(Optional.empty());
        when(taskPollParticipantRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }


    @Test
    void deletePoll_deletesExistingPoll() {
        TaskPoll existingPoll = new TaskPoll();
        existingPoll.setId(3L);
        existingPoll.setTask(task);
        when(taskPollRepository.findByTask_Id(5L)).thenReturn(Optional.of(existingPoll));

        taskPollService.deletePoll(5L);

        verify(taskPollRepository).delete(existingPoll);
    }

    @Test
    void savePoll_createsPoll() {
        TaskPollUpsertDTO dto = new TaskPollUpsertDTO();
        dto.setTitle("Review Poll");
        dto.setDescription("**Beschreibung**");
        dto.setStartDate(LocalDate.parse("2026-05-05"));
        dto.setEndDate(LocalDate.parse("2026-05-11"));
        dto.setDayStartTime(LocalTime.parse("08:00"));
        dto.setDayEndTime(LocalTime.parse("18:00"));
        dto.setSlotMinutes(30);
        dto.setIncludedDates(List.of(LocalDate.parse("2026-05-05"), LocalDate.parse("2026-05-06")));

        var result = taskPollService.savePoll(5L, dto);

        assertEquals(5L, result.getTaskId());
        assertEquals("Review Poll", result.getTitle());
        assertEquals(2, result.getIncludedDates().size());
    }
}
