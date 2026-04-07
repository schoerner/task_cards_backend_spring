package de.acosci.tasks.model.mapper;

import de.acosci.tasks.model.dto.TaskCommentResponseDTO;
import de.acosci.tasks.model.entity.TaskComment;

public final class TaskCommentMapper {

    private TaskCommentMapper() {
    }

    public static TaskCommentResponseDTO toResponseDTO(TaskComment comment) {
        if (comment == null) {
            return null;
        }

        TaskCommentResponseDTO dto = new TaskCommentResponseDTO();
        dto.setId(comment.getId());
        dto.setTaskId(comment.getTask() != null ? comment.getTask().getId() : null);
        dto.setAuthorUserId(comment.getAuthor() != null ? comment.getAuthor().getId() : null);
        dto.setAuthorEmail(comment.getAuthor() != null ? comment.getAuthor().getEmail() : null);
        dto.setAuthorFirstName(comment.getAuthor() != null ? comment.getAuthor().getFirstName() : null);
        dto.setAuthorLastName(comment.getAuthor() != null ? comment.getAuthor().getLastName() : null);
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());
        return dto;
    }
}
