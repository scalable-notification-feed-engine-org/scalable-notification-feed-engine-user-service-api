package com.activity_hub.notification_feed.api;

import com.activity_hub.notification_feed.dto.request.ProfileSaveRequestDto;
import com.activity_hub.notification_feed.dto.request.ProfileUpdateRequestDto;
import com.activity_hub.notification_feed.dto.response.ProfileResponseDto;
import com.activity_hub.notification_feed.service.impl.ProfileService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/user-service/api/v1/profiles")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PostMapping
    public ResponseEntity<String> saveProfile(@Valid @RequestBody ProfileSaveRequestDto request) {
        profileService.saveProfile(request);
        return new ResponseEntity<>("Profile initialized successfully", HttpStatus.CREATED);
    }

    @PutMapping("/me")
    public ResponseEntity<String> updateMyProfile(
            @RequestHeader("X-User-Id") String loggedInUserId,
            @Valid @RequestBody ProfileUpdateRequestDto request) {
        
        profileService.updateProfile(UUID.fromString(loggedInUserId), request);
        return ResponseEntity.ok("Profile updated successfully");
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ProfileResponseDto> getProfile(
            @PathVariable("userId") String targetUserId,
            @RequestHeader("X-User-Id") String loggedInUserId) {
        ProfileResponseDto profile = profileService.getProfile(
                UUID.fromString(targetUserId),
                UUID.fromString(loggedInUserId)
        );

        return ResponseEntity.ok(profile);
    }
}