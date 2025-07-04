package de.acosci.tasks.model.dto;

import java.util.Date;

public record TimeRecordDTO(
        Long id,
        Date timeStart,
        Date timeEnd,
        Long taskID
) {
}
