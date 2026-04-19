package de.acosci.tasks.service;

import de.acosci.tasks.model.dto.*;

public interface TaskPollService {
    java.util.List<TaskPollOwnerSummaryDTO> getOwnedPolls();
    TaskPollResponseDTO getPollByTask(Long taskId);
    TaskPollResponseDTO savePoll(Long taskId, TaskPollUpsertDTO dto);
    TaskPollResponseDTO finalizePoll(Long taskId, TaskPollFinalizeDTO dto);
    void deletePoll(Long taskId);
    TaskPollPublicResponseDTO getPublicPoll(String token);
    TaskPollPublicResponseDTO submitPublicResponse(String token, TaskPollPublicSubmissionDTO dto);
}
