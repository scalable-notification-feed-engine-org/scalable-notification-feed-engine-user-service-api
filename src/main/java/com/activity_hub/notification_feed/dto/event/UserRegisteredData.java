package com.activity_hub.notification_feed.dto.event;

public record UserRegisteredData(
        String userId,
        String email,
        String firstName,
        String lastName,
        String otp
) {}