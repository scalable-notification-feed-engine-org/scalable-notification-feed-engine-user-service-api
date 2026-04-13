package com.activity_hub.notification_feed.service;

import com.activity_hub.notification_feed.enums.FollowType;

import java.util.UUID;

public interface UserStatsService {
     void updateStats(UUID followerId, UUID followeeId, FollowType followType);
}
