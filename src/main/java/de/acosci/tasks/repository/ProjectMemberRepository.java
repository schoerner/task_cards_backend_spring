package de.acosci.tasks.repository;

import de.acosci.tasks.model.entity.ProjectMember;
import de.acosci.tasks.model.enums.ProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    List<ProjectMember> findAllByUser_Id(Long userId);
    List<ProjectMember> findAllByProject_Id(Long projectId);
    Optional<ProjectMember> findByProject_IdAndUser_Id(Long projectId, Long userId);
    boolean existsByProject_IdAndUser_Id(Long projectId, Long userId);
    long countByProject_IdAndRole(Long projectId, ProjectRole role);
}
