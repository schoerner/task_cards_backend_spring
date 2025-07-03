package de.acosci.tasks.controller.rest;

import de.acosci.tasks.model.dto.TimeRecordDTO;
import de.acosci.tasks.model.entity.Task;
import de.acosci.tasks.service.TimeRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("rest/time_records")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173/", "http://localhost:3000/"})
public class TimeRecordRestController {
    @Autowired
    private final TimeRecordService timeRecordService;

//    @PostMapping
//    public ResponseEntity<TimeRecordDTO> createTimeRecord(@RequestBody TimeRecordDTO timeRecord) {
//        return ResponseEntity.ok(timeRecordService.save(timeRecord));
//    }
//
//    @PutMapping
//    public ResponseEntity<TimeRecordDTO> updateTimeRecord(@RequestBody TimeRecordDTO timeRecord) {
//        return ResponseEntity.ok(timeRecordService.save(timeRecord));
//    }


    @PostMapping
    public ResponseEntity<TimeRecordDTO> startTimeRecord(@RequestBody Task task) {
        return ResponseEntity.ok(timeRecordService.start(task));
    }

    @PutMapping
    public ResponseEntity<TimeRecordDTO> startTimeRecord(@RequestBody TimeRecordDTO task) {
        return ResponseEntity.ok(timeRecordService.stop(task));
    }

}
