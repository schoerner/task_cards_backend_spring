package de.acosci.tasks.service;

import de.acosci.tasks.model.dto.TaskTimeDTO;
import de.acosci.tasks.model.dto.TimeRecordDTO;
import de.acosci.tasks.model.entity.Task;
import de.acosci.tasks.model.entity.TimeRecord;
import de.acosci.tasks.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TaskTimeService {
    @Autowired
    private TaskRepository taskRepository;

    private TimeRecordDTO adapt(TimeRecord timeRecord) {
        if(timeRecord == null) {
            return null;
        }
        return new TimeRecordDTO(
                timeRecord.getId(),
                timeRecord.getTimeStart(),
                timeRecord.getTimeEnd(),
                timeRecord.getTask().getId()
        );
    }

    private TaskTimeDTO adapt(Task task) {
        TimeRecord activeTimeRecord = task.getActiveTimeRecord();
        Boolean isActive = task.getTimeRecords().stream().anyMatch( timeRecord -> timeRecord.isActive());

        return new TaskTimeDTO(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                isActive,
                adapt(activeTimeRecord)
        );
    }

    public List<TaskTimeDTO> getAllTaskTimeDTO() {
        List<Task> tasks = taskRepository.findAll();
        List<TaskTimeDTO> taskTimeDTOs = new ArrayList<>();

        tasks.stream().forEach(task -> {
            taskTimeDTOs.add(adapt(task));
        });

        return taskTimeDTOs;
    }


}
