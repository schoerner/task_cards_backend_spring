package de.acosci.tasks.common.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TaskPollMailProperties.class)
public class MailConfig {
}