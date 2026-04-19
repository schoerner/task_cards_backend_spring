package de.acosci.tasks.model.dto;

import de.acosci.tasks.model.enums.TaskPollStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
public class TaskPollOwnerSummaryDTO {
    private Long id;
    private Long taskId;
    private String taskTitle;
    private Long projectId;
    private String projectName;
    private String title;
    private TaskPollStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer slotMinutes;
    private Integer participantCount;
    private Integer respondedParticipantCount;
    private OffsetDateTime finalizedStartAt;
    private OffsetDateTime finalizedEndAt;
    private OffsetDateTime updatedAt;
}
