package com.activity_hub.notification_feed.service;

import com.activity_hub.notification_feed.entity.OutboxEvent;

public interface OutboxService {
    public void updateStatus(OutboxEvent event, String status);
}
