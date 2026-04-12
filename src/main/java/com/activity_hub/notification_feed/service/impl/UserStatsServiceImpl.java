package com.activity_hub.notification_feed.service.impl;

import com.activity_hub.notification_feed.entity.Follow;
import com.activity_hub.notification_feed.entity.UserStats;
import com.activity_hub.notification_feed.exception.DuplicateEntryException;
import com.activity_hub.notification_feed.repository.FollowRepository;
import com.activity_hub.notification_feed.repository.UserStatusRepository;
import com.activity_hub.notification_feed.service.UserStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserStatsServiceImpl implements UserStatsService {

    private final UserStatusRepository userStatsRepository;
    private final FollowRepository followRepository;

    @Transactional
    public void updateStats(UUID followerId, UUID followeeId) {

        ensureUserStatsExists(followerId);

        ensureUserStatsExists(followeeId);

        Optional<Follow> follow = followRepository
                .findByIdFollowerIdAndIdFolloweeId(followerId, followeeId);

        if (follow.isPresent()) {
            throw new DuplicateEntryException("FollowerId and FolloweeId already exists");
        }

        userStatsRepository.incrementFollowingCount(followerId);

        userStatsRepository.incrementFollowerCount(followeeId);
    }

    private void ensureUserStatsExists(UUID userId) {
        if (!userStatsRepository.existsById(userId)) {
            UserStats newStats = UserStats.builder()
                    .userId(userId)
                    .followerCount(0)
                    .followingCount(0)
                    .build();
            userStatsRepository.save(newStats);
        }

    }

}