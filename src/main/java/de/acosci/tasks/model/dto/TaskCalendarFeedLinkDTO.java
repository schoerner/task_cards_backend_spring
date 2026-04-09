package de.acosci.tasks.model.dto;

import lombok.Data;

@Data
public class TaskCalendarFeedLinkDTO {
    private boolean tokenGenerated;
    private String feedUrl;
    private String tokenHint;
}