package de.acosci.tasks.security;
import de.acosci.tasks.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("projectSecurity")
@RequiredArgsConstructor
public class ProjectSecurity {

    private final ProjectRepository projectRepository;

    public boolean isOwner(Long projectId, Long userId) {
        if (projectId == null || userId == null) {
            return false;
        }
        return projectRepository.findById(projectId)
                .map(project -> project.getCreator().getId().equals(userId))
                .orElse(false);
    }

    public boolean isMember(Long projectId, Long userId) {
        if (projectId == null || userId == null) {
            return false;
        }
        return projectRepository.findById(projectId)
                .map(project -> project.getMembers().stream()
                        .anyMatch(user -> user.getId().equals(userId)))
                .orElse(false);
    }
}