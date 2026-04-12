package com.activity_hub.notification_feed.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FollowEvent {
    private UUID followerId;
    private UUID followeeId;
}