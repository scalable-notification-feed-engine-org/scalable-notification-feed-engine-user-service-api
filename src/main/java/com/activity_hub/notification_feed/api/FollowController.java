package com.activity_hub.notification_feed.api;

import com.activity_hub.notification_feed.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/follows")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping("/follow/{followeeId}")
    public ResponseEntity<String> followUser(
            @RequestHeader("X-User-Id") UUID followerId,
            @PathVariable UUID followeeId) {
        
        followService.followUser(followerId, followeeId);
        
        return ResponseEntity.ok("Follow request processed successfully.");
    }

    @PostMapping("/unfollow/{followeeId}")
    public ResponseEntity<String> unfollowUser(
            @RequestHeader("X-User-Id") UUID followerId,
            @PathVariable UUID followeeId) {

        followService.unfollowUser(followerId, followeeId);

        return ResponseEntity.ok("Follow request processed successfully.");
    }
}