package de.acosci.tasks.service;

import de.acosci.tasks.model.dto.TimeRecordDTO;
import de.acosci.tasks.model.entity.Task;
import de.acosci.tasks.model.entity.TimeRecord;
import de.acosci.tasks.repository.TaskRepository;
import de.acosci.tasks.repository.TimeRecordRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class TimeRecordService {
    @Autowired
    private TimeRecordRepository timeRecordRepository;

    @Autowired
    private TaskRepository taskRepository;

    private TimeRecordDTO adapt(TimeRecord timeRecord) {
        return new TimeRecordDTO(
                timeRecord.getId(),
                timeRecord.getTimeStart(),
                timeRecord.getTimeEnd(),
                timeRecord.getTask().getId()
        );
    }
    private TimeRecord adapt(TimeRecordDTO timeRecordDTO) {
        return new TimeRecord(
                timeRecordDTO.id(),
                timeRecordDTO.timeStart(),
                timeRecordDTO.timeEnd(),
                taskRepository.findById(timeRecordDTO.taskID())
                    .orElseThrow(() -> new EntityNotFoundException("The task with id " + timeRecordDTO.taskID() + " was not found.") )
        );
    }

    public List<TimeRecordDTO> findAllByTask(Task task) {
        List<TimeRecordDTO> dtos = new ArrayList<>();
        timeRecordRepository.findAllByTask(task).forEach(t -> dtos.add(adapt(t)));
        return dtos;
    }
    public TimeRecordDTO save(TimeRecordDTO timeRecord) {
        return adapt( timeRecordRepository.save( adapt(timeRecord) ) );
    }
    public void delete(TimeRecordDTO timeRecord) {
        timeRecordRepository.delete( adapt(timeRecord) );
    }
    public void deleteById(Long id){
        timeRecordRepository.deleteById(id);
    }

    public TimeRecordDTO start(Task task) {
        return adapt( timeRecordRepository.save( new TimeRecord(task) ) );
    }
    public TimeRecordDTO stop(TimeRecordDTO timeRecordDTO) {
        TimeRecord timeRecord = adapt(timeRecordDTO);
        timeRecord.stop();
        return adapt( timeRecordRepository.save( timeRecord ) );
    }
}
