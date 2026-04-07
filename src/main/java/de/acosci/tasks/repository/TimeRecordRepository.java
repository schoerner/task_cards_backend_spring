package de.acosci.tasks.repository;

import de.acosci.tasks.model.entity.TimeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TimeRecordRepository extends JpaRepository<TimeRecord, Long> {

    List<TimeRecord> findAllByTask_IdOrderByTimeStartDesc(Long taskId);

    Optional<TimeRecord> findFirstByTask_IdAndTimeEndIsNullOrderByTimeStartDesc(Long taskId);
}