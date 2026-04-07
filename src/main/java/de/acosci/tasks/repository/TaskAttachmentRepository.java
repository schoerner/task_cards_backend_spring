package de.acosci.tasks.repository;

import de.acosci.tasks.model.entity.TaskAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, Long> {
    List<TaskAttachment> findAllByTask_Id(Long taskId);
}
