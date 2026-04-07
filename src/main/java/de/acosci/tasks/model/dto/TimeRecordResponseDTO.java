package de.acosci.tasks.model.dto;

import lombok.Data;

import java.util.Date;

@Data
public class TimeRecordResponseDTO {
    private Long id;
    private Date timeStart;
    private Date timeEnd;
    private Long taskId;
}
