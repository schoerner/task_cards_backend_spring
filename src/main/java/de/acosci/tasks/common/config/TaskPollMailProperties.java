package de.acosci.tasks.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.poll")
public class TaskPollMailProperties {

    private String publicBaseUrl;
    private final Mail mail = new Mail();

    @Getter
    @Setter
    public static class Mail {
        private String from;
        private String fromName;
        private String replyTo;
    }
}