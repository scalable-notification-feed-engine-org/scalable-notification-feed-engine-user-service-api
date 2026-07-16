package com.activity_hub.notification_feed.service.impl;

import com.activity_hub.notification_feed.dto.request.ProfileSaveRequestDto;
import com.activity_hub.notification_feed.dto.request.ProfileUpdateRequestDto;
import com.activity_hub.notification_feed.dto.response.ProfileResponseDto;
import com.activity_hub.notification_feed.entity.User;
import com.activity_hub.notification_feed.entity.UserProfile;
import com.activity_hub.notification_feed.exception.NotFoundException;
import com.activity_hub.notification_feed.repository.UserProfileRepository;
import com.activity_hub.notification_feed.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
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

        User user = userRepository.findById(UUID.fromString(request.getId()))
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getId()));

        UUID actualPk = UUID.fromString(user.getKeycloakId());

        if (profileRepository.existsById(actualPk)) {
            throw new IllegalStateException("Profile already exists for this user");
        }

        UserProfile profile = new UserProfile();
        profile.setId(actualPk);
        profile.setName(request.getName());
        profile.setAliasName(request.getAliasName());
        profile.setBioLines(request.getBioLines());
        profile.setCategory(request.getCategory());
        profile.setLocation(request.getLocation());
        profile.setAvatarImageKey(request.getAvatarImageKey());
        profile.setCoverImageKey(request.getCoverImageKey());
        profile.setVerified(false);
        profile.setUser(user);

        user.setUserProfile(profile);

        profileRepository.save(profile);
    }

    public void updateProfile(UUID userId, ProfileUpdateRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with"));

        UUID actualPk = UUID.fromString(user.getKeycloakId());

        UserProfile profile = user.getUserProfile();
        if (profile == null) {
            profile = new UserProfile();
            profile.setId(actualPk);
            profile.setUser(user);
            profile.setVerified(false);
            user.setUserProfile(profile);
        }

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            String fullName = request.getName().trim();
            int firstSpaceIndex = fullName.indexOf(" ");

            if (firstSpaceIndex != -1) {
                user.setFirstName(fullName.substring(0, firstSpaceIndex));
                user.setLastName(fullName.substring(firstSpaceIndex + 1));
            } else {
                user.setFirstName(fullName);
                user.setLastName("");
            }
        }

        if (request.getName() != null) profile.setName(request.getName());
        if (request.getAliasName() != null) profile.setAliasName(request.getAliasName());
        if (request.getBioLines() != null) profile.setBioLines(request.getBioLines());
        if (request.getCategory() != null) profile.setCategory(request.getCategory());
        if (request.getLocation() != null) profile.setLocation(request.getLocation());

        if (request.getAvatarImageKey() != null && !request.getAvatarImageKey().startsWith("blob:")) {
            profile.setAvatarImageKey(request.getAvatarImageKey());
        }

        if (request.getCoverImageKey() != null && !request.getCoverImageKey().startsWith("blob:")) {
            profile.setCoverImageKey(request.getCoverImageKey());
        }

    }

    @Transactional(readOnly = true)
    public ProfileResponseDto getProfile(UUID targetUserId, UUID currentUserId) {
        UserProfile profile = profileRepository.findById(targetUserId)
                .orElseThrow(() -> new NotFoundException("Profile not found for ID: " + targetUserId));

        long followers = 0;
        long following = 0;

        if (profile.getUser().getStats() != null) {
            followers = profile.getUser().getStats().getFollowerCount();
            following = profile.getUser().getStats().getFollowingCount();
        }

        String avatarUrl = (profile.getAvatarImageKey() != null)
                ? cdnBaseUrl + profile.getAvatarImageKey()
                : "";

        String coverUrl = (profile.getCoverImageKey() != null)
                ? cdnBaseUrl + profile.getCoverImageKey()
                : "";

        return ProfileResponseDto.builder()
                .id(profile.getId().toString())
                .name(profile.getName())
                .aliasName(profile.getAliasName())
                .isVerified(profile.isVerified())
                .avatarImageUrl(avatarUrl)
                .coverImageUrl(coverUrl)
                .followersCount(followers)
                .followingCount(following)
                .bioLines(profile.getBioLines() != null ? new ArrayList<>(profile.getBioLines()) : new ArrayList<>())
                .category(profile.getCategory())
                .location(profile.getLocation())
                .isOwnProfile(targetUserId.equals(currentUserId))
                .build();
    }
}