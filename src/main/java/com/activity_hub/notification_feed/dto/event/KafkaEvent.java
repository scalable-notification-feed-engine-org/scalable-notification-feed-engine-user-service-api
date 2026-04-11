package com.activity_hub.notification_feed.dto.event;

import java.time.LocalDateTime;

public record KafkaEvent<T>(
        String event_type,
        LocalDateTime occurred_at,
        T data
) {}

