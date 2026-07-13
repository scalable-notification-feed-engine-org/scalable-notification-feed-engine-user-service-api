package com.activity_hub.notification_feed.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponseDto {
    private String id;
    private String name;
    private String aliasName;
    private boolean isVerified;
    private String coverImageUrl;
    private String avatarImageUrl;
    private long followersCount;
    private long followingCount;
    private List<String> bioLines;
    private String category;
    private String location;
    private boolean isOwnProfile;
}