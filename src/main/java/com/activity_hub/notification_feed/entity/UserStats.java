package com.activity_hub.notification_feed.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "user_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStats {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "follower_count", nullable = false)
    private long followerCount = 0;

    @Column(name = "following_count", nullable = false)
    private long followingCount = 0;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;


}