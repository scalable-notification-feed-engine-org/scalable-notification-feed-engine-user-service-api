package com.activity_hub.notification_feed.repository;

import com.activity_hub.notification_feed.entity.Follow;
import com.activity_hub.notification_feed.entity.FollowId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface FollowRepository extends JpaRepository<Follow, FollowId> {
    Optional<Follow> findByIdFollowerIdAndIdFolloweeId(UUID followerId, UUID followeeId);
}
