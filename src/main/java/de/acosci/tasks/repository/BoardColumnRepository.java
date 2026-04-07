package de.acosci.tasks.repository;

import de.acosci.tasks.model.entity.BoardColumn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BoardColumnRepository extends JpaRepository<BoardColumn, Long> {
    List<BoardColumn> findAllByProject_IdOrderByPositionAsc(Long projectId);
    Optional<BoardColumn> findByProject_IdAndName(Long projectId, String name);
}
