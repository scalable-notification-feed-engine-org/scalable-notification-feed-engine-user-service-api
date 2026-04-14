package com.activity_hub.notification_feed.dto.response;

import java.util.UUID;

public record UserMetadataResponseDTO(
    UUID userId,
    String firstName,
    String lastName,
    String profilePicUrl
) {}