package de.acosci.tasks.service.impl;

import de.acosci.tasks.model.dto.*;
import de.acosci.tasks.model.entity.*;
import de.acosci.tasks.model.enums.TaskPollAvailabilityStatus;
import de.acosci.tasks.model.enums.TaskPollStatus;
import de.acosci.tasks.repository.*;
import de.acosci.tasks.service.TaskPollService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskPollServiceImpl implements TaskPollService {

    private final TaskPollRepository taskPollRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskPollParticipantRepository participantRepository;
    private final TaskPollAvailabilityRepository availabilityRepository;


    @Override
    @Transactional(readOnly = true)
    public List<TaskPollOwnerSummaryDTO> getOwnedPolls() {
        User currentUser = getCurrentUser();
        return taskPollRepository.findAllByCreatedBy_IdOrderByUpdatedAtDesc(currentUser.getId()).stream()
                .map(this::toOwnerSummaryDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TaskPollResponseDTO getPollByTask(Long taskId) {
        TaskPoll poll = taskPollRepository.findByTask_Id(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task poll not found for task: " + taskId));
        return toOwnerDto(poll, getCurrentUser());
    }

    @Override
    public TaskPollResponseDTO savePoll(Long taskId, TaskPollUpsertDTO dto) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));

        User currentUser = getCurrentUser();

        TaskPoll poll = taskPollRepository.findByTask_Id(taskId).orElseGet(TaskPoll::new);
        if (poll.getId() == null) {
            poll.setTask(task);
            poll.setCreatedBy(currentUser);
        }

        poll.setTitle(dto.getTitle());
        poll.setDescription(dto.getDescription());
        poll.setStartDate(dto.getStartDate());
        poll.setEndDate(dto.getEndDate());
        poll.setDayStartTime(dto.getDayStartTime());
        poll.setDayEndTime(dto.getDayEndTime());
        poll.setSlotMinutes(dto.getSlotMinutes());
        syncIncludedDates(poll, dto.getIncludedDates());
        syncParticipants(poll, dto.getParticipants(), currentUser);

        TaskPoll savedPoll = taskPollRepository.save(poll);
        syncOwnerResponse(savedPoll, currentUser, dto.getOwnerResponse());

        TaskPoll reloadedPoll = taskPollRepository.findById(savedPoll.getId())
                .orElseThrow(() -> new IllegalStateException("Saved task poll could not be reloaded: " + savedPoll.getId()));

        return toOwnerDto(reloadedPoll, currentUser);
    }

    @Override
    public TaskPollResponseDTO finalizePoll(Long taskId, TaskPollFinalizeDTO dto) {
        TaskPoll poll = taskPollRepository.findByTask_Id(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task poll not found for task: " + taskId));
        poll.setFinalizedStartAt(dto.getFinalizedStartAt());
        poll.setFinalizedEndAt(dto.getFinalizedEndAt());
        poll.setStatus(TaskPollStatus.FINALIZED);

        Task task = poll.getTask();
        task.setStartAt(dto.getFinalizedStartAt());
        task.setDueDate(dto.getFinalizedEndAt());
        taskRepository.save(task);

        return toOwnerDto(taskPollRepository.save(poll), getCurrentUser());
    }

    @Override
    public void deletePoll(Long taskId) {
        TaskPoll poll = taskPollRepository.findByTask_Id(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task poll not found for task: " + taskId));
        taskPollRepository.delete(poll);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskPollPublicResponseDTO getPublicPoll(String token) {
        TaskPollParticipant participant = participantRepository.findByInvitationTokenHash(hashToken(token))
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found."));
        return toPublicDto(participant);
    }

    @Override
    public TaskPollPublicResponseDTO submitPublicResponse(String token, TaskPollPublicSubmissionDTO dto) {
        TaskPollParticipant participant = participantRepository.findByInvitationTokenHash(hashToken(token))
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found."));
        participant.setResponseName(dto.getDisplayName());
        participant.setRespondedAt(OffsetDateTime.now());
        replaceAvailabilities(participant, dto.getResponse());
        return toPublicDto(participantRepository.save(participant));
    }

    private void syncIncludedDates(TaskPoll poll, List<LocalDate> includedDates) {
        poll.getDates().clear();
        for (LocalDate includedDate : includedDates) {
            TaskPollDate date = new TaskPollDate();
            date.setPoll(poll);
            date.setPollDate(includedDate);
            poll.getDates().add(date);
        }
    }

    private void syncParticipants(TaskPoll poll, List<TaskPollParticipantDTO> participants, User currentUser) {
        Map<Long, TaskPollParticipant> existingById = poll.getParticipants().stream()
                .filter(p -> p.getId() != null)
                .collect(Collectors.toMap(TaskPollParticipant::getId, p -> p));

        Set<Long> seenInternalUserIds = new HashSet<>();
        Set<String> seenExternalEmails = new HashSet<>();

        Set<TaskPollParticipant> preservedOwnerParticipants = poll.getParticipants().stream()
                .filter(p -> p.getUser() != null && Objects.equals(p.getUser().getId(), currentUser.getId()))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<TaskPollParticipant> nextParticipants = new LinkedHashSet<>(preservedOwnerParticipants);

        for (TaskPollParticipantDTO dto : participants) {
            TaskPollParticipant participant = dto.getId() != null ? existingById.getOrDefault(dto.getId(), new TaskPollParticipant()) : new TaskPollParticipant();
            participant.setPoll(poll);
            participant.setDisplayName(dto.getDisplayName());
            participant.setResponseName(dto.getResponseName());

            if ("internal".equalsIgnoreCase(dto.getType()) && dto.getUserId() != null) {
                Long internalUserId = dto.getUserId();

                if (Objects.equals(internalUserId, currentUser.getId())) {
                    continue;
                }

                if (!seenInternalUserIds.add(internalUserId)) {
                    continue;
                }

                User user = userRepository.findById(internalUserId)
                        .orElseThrow(() -> new IllegalArgumentException("User not found: " + internalUserId));

                participant.setUser(user);
                participant.setDisplayName(resolveInternalParticipantDisplayName(user));
                participant.setExternalEmail(resolveInternalParticipantEmail(user));

                ensureInvitationToken(participant);
            } else {
                String normalizedEmail = dto.getEmail() != null ? dto.getEmail().trim().toLowerCase() : null;

                if (normalizedEmail == null || normalizedEmail.isBlank()) {
                    continue;
                }

                if (!seenExternalEmails.add(normalizedEmail)) {
                    continue;
                }

                participant.setUser(null);
                participant.setDisplayName(dto.getDisplayName());
                participant.setExternalEmail(normalizedEmail);

                ensureInvitationToken(participant);
            }

            replaceAvailabilities(participant, dto.getResponse());
            nextParticipants.add(participant);
        }
        poll.getParticipants().clear();
        poll.getParticipants().addAll(nextParticipants);
    }

    private void syncOwnerResponse(TaskPoll poll, User currentUser, List<TaskPollAvailabilitySelectionDTO> ownerResponse) {
        TaskPollParticipant ownerParticipant = participantRepository.findByPoll_IdAndUser_Id(poll.getId(), currentUser.getId())
                .orElseGet(() -> {
                    TaskPollParticipant participant = new TaskPollParticipant();
                    participant.setPoll(poll);
                    participant.setUser(currentUser);
                    participant.setDisplayName(resolveInternalParticipantDisplayName(currentUser));
                    participant.setExternalEmail(resolveInternalParticipantEmail(currentUser));
                    ensureInvitationToken(participant);
                    return participant;
                });
        replaceAvailabilities(ownerParticipant, ownerResponse);
        participantRepository.save(ownerParticipant);
    }

    private void replaceAvailabilities(TaskPollParticipant participant, List<TaskPollAvailabilitySelectionDTO> selections) {
        Map<OffsetDateTime, TaskPollAvailabilitySelectionDTO> incomingBySlot = new LinkedHashMap<>();
        for (TaskPollAvailabilitySelectionDTO selection : selections) {
            if (selection == null || selection.getSlotStartAt() == null || selection.getAvailability() == null) {
                continue;
            }
            incomingBySlot.put(selection.getSlotStartAt(), selection);
        }

        Map<OffsetDateTime, TaskPollAvailability> existingBySlot = participant.getAvailabilities().stream()
                .collect(Collectors.toMap(
                        TaskPollAvailability::getSlotStartAt,
                        availability -> availability,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));

        participant.getAvailabilities().removeIf(existing -> !incomingBySlot.containsKey(existing.getSlotStartAt()));

        for (Map.Entry<OffsetDateTime, TaskPollAvailabilitySelectionDTO> entry : incomingBySlot.entrySet()) {
            OffsetDateTime slotStartAt = entry.getKey();
            TaskPollAvailabilitySelectionDTO selection = entry.getValue();

            TaskPollAvailability existing = existingBySlot.get(slotStartAt);
            if (existing != null) {
                existing.setAvailability(selection.getAvailability());
            } else {
                TaskPollAvailability availability = new TaskPollAvailability();
                availability.setParticipant(participant);
                availability.setSlotStartAt(slotStartAt);
                availability.setAvailability(selection.getAvailability());
                participant.getAvailabilities().add(availability);
            }
        }
    }

    private TaskPollOwnerSummaryDTO toOwnerSummaryDto(TaskPoll poll) {
        TaskPollOwnerSummaryDTO dto = new TaskPollOwnerSummaryDTO();
        dto.setId(poll.getId());
        dto.setTaskId(poll.getTask() != null ? poll.getTask().getId() : null);
        dto.setTaskTitle(poll.getTask() != null ? poll.getTask().getTitle() : null);
        dto.setProjectId(poll.getTask() != null && poll.getTask().getProject() != null ? poll.getTask().getProject().getId() : null);
        dto.setProjectName(poll.getTask() != null && poll.getTask().getProject() != null ? poll.getTask().getProject().getName() : null);
        dto.setTitle(poll.getTitle());
        dto.setStatus(poll.getStatus());
        dto.setStartDate(poll.getStartDate());
        dto.setEndDate(poll.getEndDate());
        dto.setSlotMinutes(poll.getSlotMinutes());
        dto.setParticipantCount(poll.getParticipants() != null ? poll.getParticipants().size() : 0);
        dto.setRespondedParticipantCount((int) poll.getParticipants().stream()
                .filter(participant -> participant.getRespondedAt() != null)
                .count());
        dto.setFinalizedStartAt(poll.getFinalizedStartAt());
        dto.setFinalizedEndAt(poll.getFinalizedEndAt());
        dto.setUpdatedAt(poll.getUpdatedAt());
        return dto;
    }

    private TaskPollResponseDTO toOwnerDto(TaskPoll poll, User currentUser) {
        TaskPollResponseDTO dto = new TaskPollResponseDTO();
        dto.setId(poll.getId());
        dto.setTaskId(poll.getTask().getId());
        dto.setTitle(poll.getTitle());
        dto.setDescription(poll.getDescription());
        dto.setStartDate(poll.getStartDate());
        dto.setEndDate(poll.getEndDate());
        dto.setDayStartTime(poll.getDayStartTime());
        dto.setDayEndTime(poll.getDayEndTime());
        dto.setSlotMinutes(poll.getSlotMinutes());
        dto.setIncludedDates(poll.getDates().stream().map(TaskPollDate::getPollDate).sorted().toList());
        dto.setParticipants(poll.getParticipants().stream()
                .filter(p -> p.getUser() == null || !Objects.equals(p.getUser().getId(), currentUser.getId()))
                .map(this::toParticipantDto)
                .toList());
        dto.setOwnerResponse(resolveOwnerResponse(poll, currentUser));
        dto.setHeatmap(buildHeatmap(poll, currentUser));
        dto.setFinalizedStartAt(poll.getFinalizedStartAt());
        dto.setFinalizedEndAt(poll.getFinalizedEndAt());
        return dto;
    }

    private List<TaskPollAvailabilitySelectionDTO> resolveOwnerResponse(TaskPoll poll, User currentUser) {
        return poll.getParticipants().stream()
                .filter(p -> p.getUser() != null && Objects.equals(p.getUser().getId(), currentUser.getId()))
                .findFirst()
                .map(p -> p.getAvailabilities().stream().map(this::toAvailabilityDto).toList())
                .orElse(List.of());
    }

    private TaskPollParticipantDTO toParticipantDto(TaskPollParticipant participant) {
        TaskPollParticipantDTO dto = new TaskPollParticipantDTO();
        dto.setId(participant.getId());
        dto.setType(participant.getUser() != null ? "internal" : "external");
        dto.setUserId(participant.getUser() != null ? participant.getUser().getId() : null);
        dto.setDisplayName(resolveParticipantName(participant));
        dto.setEmail(participant.getUser() != null
                ? resolveInternalParticipantEmail(participant.getUser())
                : participant.getExternalEmail());
        dto.setResponseName(participant.getResponseName());
        dto.setInvitedAt(participant.getInvitedAt());
        dto.setRespondedAt(participant.getRespondedAt());
        dto.setLastReminderAt(participant.getLastReminderAt());
        dto.setResponse(participant.getAvailabilities().stream().map(this::toAvailabilityDto).toList());
        return dto;
    }

    private TaskPollAvailabilitySelectionDTO toAvailabilityDto(TaskPollAvailability availability) {
        TaskPollAvailabilitySelectionDTO dto = new TaskPollAvailabilitySelectionDTO();
        dto.setSlotStartAt(availability.getSlotStartAt());
        dto.setAvailability(availability.getAvailability());
        return dto;
    }

    private List<TaskPollHeatmapSlotDTO> buildHeatmap(TaskPoll poll, User currentUser) {
        List<OffsetDateTime> slots = createSlots(poll);
        List<TaskPollParticipant> allParticipants = new ArrayList<>(poll.getParticipants());

        return slots.stream().map(slot -> {
            TaskPollHeatmapSlotDTO dto = new TaskPollHeatmapSlotDTO();
            dto.setSlotStartAt(slot);
            for (TaskPollParticipant participant : allParticipants) {
                TaskPollAvailabilityStatus status = participant.getAvailabilities().stream()
                        .filter(a -> a.getSlotStartAt() != null && slot != null && a.getSlotStartAt().toInstant().equals(slot.toInstant()))
                        .map(TaskPollAvailability::getAvailability)
                        .findFirst()
                        .orElse(TaskPollAvailabilityStatus.UNAVAILABLE);
                String name = resolveParticipantName(participant);
                if (status == TaskPollAvailabilityStatus.AVAILABLE) {
                    dto.getAvailableNames().add(name);
                } else if (status == TaskPollAvailabilityStatus.IF_NEEDED) {
                    dto.getIfNeededNames().add(name);
                } else {
                    dto.getUnavailableNames().add(name);
                }
            }
            dto.setAvailableCount(dto.getAvailableNames().size());
            dto.setIfNeededCount(dto.getIfNeededNames().size());
            dto.setUnavailableCount(dto.getUnavailableNames().size());
            dto.setScore(dto.getAvailableCount() * 2 + dto.getIfNeededCount());
            return dto;
        }).toList();
    }

    private List<TaskPollHeatmapSlotDTO> buildPublicHeatmap(TaskPoll poll) {
        return buildHeatmap(poll, null).stream()
                .map(slot -> {
                    TaskPollHeatmapSlotDTO dto = new TaskPollHeatmapSlotDTO();
                    dto.setSlotStartAt(slot.getSlotStartAt());
                    dto.setAvailableCount(slot.getAvailableCount());
                    dto.setIfNeededCount(slot.getIfNeededCount());
                    dto.setUnavailableCount(slot.getUnavailableCount());
                    dto.setScore(slot.getScore());
                    dto.setAvailableNames(new ArrayList<>());
                    dto.setIfNeededNames(new ArrayList<>());
                    dto.setUnavailableNames(new ArrayList<>());
                    return dto;
                })
                .toList();
    }

    private List<OffsetDateTime> createSlots(TaskPoll poll) {
        List<OffsetDateTime> slots = new ArrayList<>();
        LocalTime current;
        List<LocalDate> dates = poll.getDates().stream().map(TaskPollDate::getPollDate).sorted().toList();
        for (LocalDate date : dates) {
            current = poll.getDayStartTime();
            while (current.isBefore(poll.getDayEndTime())) {
                ZoneOffset offset = ZoneId.systemDefault()
                        .getRules()
                        .getOffset(LocalDateTime.of(date, current));
                slots.add(OffsetDateTime.of(date, current, offset));
                current = current.plusMinutes(poll.getSlotMinutes());
            }
        }
        return slots;
    }

    private TaskPollPublicResponseDTO toPublicDto(TaskPollParticipant participant) {
        TaskPoll poll = participant.getPoll();
        TaskPollPublicResponseDTO dto = new TaskPollPublicResponseDTO();
        dto.setParticipantId(participant.getId());
        dto.setDisplayName(resolveParticipantName(participant));
        dto.setTaskId(poll.getTask().getId());
        dto.setTitle(poll.getTitle());
        dto.setDescription(poll.getDescription());
        dto.setStartDate(poll.getStartDate());
        dto.setEndDate(poll.getEndDate());
        dto.setDayStartTime(poll.getDayStartTime());
        dto.setDayEndTime(poll.getDayEndTime());
        dto.setSlotMinutes(poll.getSlotMinutes());
        dto.setIncludedDates(poll.getDates().stream().map(TaskPollDate::getPollDate).sorted().toList());
        dto.setResponse(participant.getAvailabilities().stream().map(this::toAvailabilityDto).toList());
        dto.setHeatmap(buildPublicHeatmap(poll));
        return dto;
    }

    private String resolveParticipantName(TaskPollParticipant participant) {
        if (participant.getResponseName() != null && !participant.getResponseName().isBlank()) {
            return participant.getResponseName();
        }
        if (participant.getDisplayName() != null && !participant.getDisplayName().isBlank()) {
            return participant.getDisplayName();
        }
        if (participant.getUser() != null) {
            return resolveInternalParticipantDisplayName(participant.getUser());
        }
        return participant.getExternalEmail();
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte aByte : bytes) {
                builder.append(String.format("%02x", aByte));
            }
            return builder.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Could not hash token.", ex);
        }
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + email));
    }

    private String resolveInternalParticipantDisplayName(User user) {
        if (user == null) {
            return null;
        }

        if (user.getProfile() != null && hasText(user.getProfile().getName())) {
            return user.getProfile().getName();
        }

        String fullName = joinNonBlank(user.getFirstName(), user.getLastName());
        if (hasText(fullName)) {
            return fullName;
        }

        return "User " + user.getId();
    }

    private String resolveInternalParticipantEmail(User user) {
        if (user == null) {
            return null;
        }

        if (user.getProfile() != null && hasText(user.getProfile().getContactEmail())) {
            return user.getProfile().getContactEmail();
        }

        return user.getEmail();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String joinNonBlank(String... values) {
        return Arrays.stream(values)
                .filter(this::hasText)
                .map(String::trim)
                .collect(Collectors.joining(" "));
    }

    private void ensureInvitationToken(TaskPollParticipant participant) {
        if (participant.getInvitationToken() == null || participant.getInvitationToken().isBlank()) {
            String rawToken = UUID.randomUUID().toString();
            participant.setInvitationToken(rawToken);
            participant.setInvitationTokenHash(hashToken(rawToken));
        }
    }

    private String buildPublicPollLink(TaskPollParticipant participant, String publicBaseUrl) {
        return publicBaseUrl + "/polls/respond/" + participant.getInvitationToken();
    }
}
