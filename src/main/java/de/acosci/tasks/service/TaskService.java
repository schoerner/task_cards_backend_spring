package de.acosci.tasks.service;

import de.acosci.tasks.model.dto.TaskCreateDTO;
import de.acosci.tasks.model.dto.TaskResponseDTO;
import de.acosci.tasks.model.dto.TaskUpdateDTO;
import de.acosci.tasks.model.dto.TimeRecordResponseDTO;

import java.util.List;

/**
 * Service for task lifecycle and kanban operations.
 */
public interface TaskService {
    List<TaskResponseDTO> getTasksByProject(Long projectId);
    TaskResponseDTO getTaskById(Long taskId);
    TaskResponseDTO createTask(TaskCreateDTO dto);
    TaskResponseDTO updateTask(Long taskId, TaskUpdateDTO dto);
    TaskResponseDTO moveTask(Long taskId, Long boardColumnId);
    TaskResponseDTO archiveTask(Long taskId);
    TaskResponseDTO restoreTask(Long taskId);
    void deleteTask(Long taskId);

    List<TimeRecordResponseDTO> getTimeRecords(Long taskId);
    TimeRecordResponseDTO getActiveTimeRecord(Long taskId);
    boolean isActive(Long taskId);
    TaskResponseDTO startTimeTracking(Long taskId);
    TaskResponseDTO stopTimeTracking(Long taskId);
}
