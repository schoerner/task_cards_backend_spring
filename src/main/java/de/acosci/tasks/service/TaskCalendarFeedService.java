package de.acosci.tasks.service;

import de.acosci.tasks.model.dto.TaskCalendarFeedLinkDTO;

public interface TaskCalendarFeedService {
    TaskCalendarFeedLinkDTO getFeedLinkForCurrentUser();
    TaskCalendarFeedLinkDTO regenerateFeedLinkForCurrentUser();
    String renderCalendarFeedByToken(String token);
}