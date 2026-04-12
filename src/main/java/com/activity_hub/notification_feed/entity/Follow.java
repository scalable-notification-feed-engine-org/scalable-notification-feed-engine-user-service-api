package com.activity_hub.notification_feed.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "followers", indexes = {
        @Index(name = "idx_followee_id", columnList = "followeeId")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Follow {
    @EmbeddedId
    private FollowId id;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}