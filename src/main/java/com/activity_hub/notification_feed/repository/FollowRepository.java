package com.activity_hub.notification_feed.repository;

import com.activity_hub.notification_feed.entity.Follow;
import com.activity_hub.notification_feed.entity.FollowId;
import com.activity_hub.notification_feed.enums.FollowType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface FollowRepository extends JpaRepository<Follow, FollowId> {

    Optional<Follow> findByIdFollowerIdAndIdFolloweeId(UUID followerId, UUID followeeId);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Follow f " +
            "WHERE f.id.followerId = :followerId AND f.id.followeeId = :followeeId AND f.followType = :followType")
    boolean existsByFollowerIdAndFolloweeIdAndFollowType(
            @Param("followerId") UUID followerId,
            @Param("followeeId") UUID followeeId,
            @Param("followType") FollowType followType);
}