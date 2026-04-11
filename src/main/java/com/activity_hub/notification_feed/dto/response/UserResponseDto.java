package com.activity_hub.notification_feed.dto.response;

import com.activity_hub.notification_feed.enums.UserRole;
import com.activity_hub.notification_feed.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String contact;
    private UserRole role;
    private Boolean emailVerified;
    private UserStatus status;
    private String avatarUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
