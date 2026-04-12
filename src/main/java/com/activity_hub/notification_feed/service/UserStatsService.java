package com.activity_hub.notification_feed.service;

import java.util.UUID;

public interface UserStatsService {
    public void updateStats(UUID followerId, UUID followeeId);
}
