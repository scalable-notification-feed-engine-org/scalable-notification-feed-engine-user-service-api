package com.activity_hub.notification_feed.entity;

import com.activity_hub.notification_feed.enums.FollowType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "followers", indexes = {
        @Index(name = "idx_followee_id", columnList = "followeeId")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Follow {

    @EmbeddedId
    private FollowId id;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "follow_type", nullable = false)
    private FollowType followType;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}