package com.activity_hub.notification_feed.repository;

import com.activity_hub.notification_feed.entity.UserStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.UUID;

public interface UserStatusRepository extends JpaRepository<UserStats,UUID> {

    @Modifying
    @Query("UPDATE UserStats s SET s.followerCount=s.followerCount+1 WHERE s.userId=:userId")
    void incrementFollowerCount(UUID userId);

}
