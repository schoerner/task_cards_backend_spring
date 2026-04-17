package de.acosci.tasks.repository;

import de.acosci.tasks.model.entity.UserFavoriteTask;
import de.acosci.tasks.model.entity.UserFavoriteTaskId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserFavoriteTaskRepository extends JpaRepository<UserFavoriteTask, UserFavoriteTaskId> {

    boolean existsByUser_IdAndTask_Id(Long userId, Long taskId);

    Optional<UserFavoriteTask> findByUser_IdAndTask_Id(Long userId, Long taskId);

    List<UserFavoriteTask> findAllByUser_Id(Long userId);
}