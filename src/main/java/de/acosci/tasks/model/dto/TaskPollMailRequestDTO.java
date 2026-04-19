package de.acosci.tasks.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskPollMailRequestDTO {
    private String subject;
    private String messageMarkdown;
    private String messageHtml;
    private String messageText;
}
