package com.activity_hub.notification_feed.service.impl;

import com.activity_hub.notification_feed.dto.request.ProfileSaveRequestDto;
import com.activity_hub.notification_feed.dto.request.ProfileUpdateRequestDto;
import com.activity_hub.notification_feed.dto.response.ProfileResponseDto;
import com.activity_hub.notification_feed.entity.User;
import com.activity_hub.notification_feed.entity.UserProfile;
import com.activity_hub.notification_feed.repository.UserRepository;
import com.activity_hub.notification_feed.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@Transactional
public class ProfileService {

    @Value("${aws.cloudfront.base-url}")
    private String cdnBaseUrl;

    private final UserProfileRepository profileRepository;
    private final UserRepository userRepository;

    public ProfileService(UserProfileRepository profileRepository, UserRepository userRepository) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
    }

    public void saveProfile(ProfileSaveRequestDto request) {
        UUID userUuid = UUID.fromString(request.getId());

        if (profileRepository.existsById(userUuid)) {
            throw new IllegalStateException("Profile already exists for this user ID");
        }

        User user = userRepository.findById(userUuid)
                .orElseThrow(() -> new RuntimeException("Core User not found with ID: " + userUuid));

        UserProfile newProfile = UserProfile.builder()
                .id(userUuid)
                .name(request.getName())
                .aliasName(request.getAliasName())
                .bioLines(request.getBioLines())
                .category(request.getCategory())
                .location(request.getLocation())
                .avatarImageKey(request.getAvatarImageKey())
                .coverImageKey(request.getCoverImageKey())
                .isVerified(false)
                .user(user)
                .build();

        profileRepository.save(newProfile);
    }

    public void updateProfile(UUID userId, ProfileUpdateRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        UserProfile profile = profileRepository.findById(userId)
                .orElseGet(() -> {
                    UserProfile newProfile = new UserProfile();
                    newProfile.setId(userId);
                    newProfile.setUser(user);
                    return newProfile;
                });

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            String fullName = request.getName().trim();
            String firstName = "";
            String lastName = "";

            int firstSpaceIndex = fullName.indexOf(" ");
            if (firstSpaceIndex != -1) {
                firstName = fullName.substring(0, firstSpaceIndex);
                lastName = fullName.substring(firstSpaceIndex + 1);
            } else {

                firstName = fullName;
                lastName = "";
            }

            user.setFirstName(firstName);
            user.setLastName(lastName);

        }
        profile.setName(request.getName());
        profile.setAliasName(request.getAliasName());
        profile.setBioLines(request.getBioLines());
        profile.setCategory(request.getCategory());
        profile.setLocation(request.getLocation());

        if (request.getAvatarImageKey() != null) {
            profile.setAvatarImageKey(request.getAvatarImageKey());
        }
        if (request.getCoverImageKey() != null) {
            profile.setCoverImageKey(request.getCoverImageKey());
        }

        profileRepository.save(profile);
    }

    @Transactional(readOnly = true)
    public ProfileResponseDto getProfile(UUID targetUserId, UUID currentUserId) {
        UserProfile profile = profileRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Profile not found for ID: " + targetUserId));

        long followers = 0;
        long javaFollowing = 0;
        if (profile.getUser().getStats() != null) {
             followers = profile.getUser().getStats().getFollowerCount();
        }
        
        String avatarUrl = (profile.getAvatarImageKey() != null)
                ? cdnBaseUrl + profile.getAvatarImageKey()
                : "https://picsum.photos/seed/voxa-avatar/200/200";

        String coverUrl = (profile.getCoverImageKey() != null)
                ? cdnBaseUrl + profile.getCoverImageKey()
                : "https://picsum.photos/seed/voxa-cover/1600/500";


        return ProfileResponseDto.builder()
                .id(profile.getId().toString())
                .name(profile.getName())
                .aliasName(profile.getAliasName())
                .isVerified(profile.isVerified())
                .avatarImageUrl(avatarUrl)
                .coverImageUrl(coverUrl)
                .followersCount(followers)
                .followingCount(javaFollowing)
                .bioLines(profile.getBioLines())
                .category(profile.getCategory())
                .location(profile.getLocation())
                .isOwnProfile(targetUserId.equals(currentUserId))
                .build();
    }
}