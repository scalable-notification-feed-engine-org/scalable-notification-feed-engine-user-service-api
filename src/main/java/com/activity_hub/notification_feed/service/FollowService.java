package com.activity_hub.notification_feed.service;

import java.util.UUID;

public interface FollowService {
    void followUser(UUID followerId, UUID followeeId);
}
