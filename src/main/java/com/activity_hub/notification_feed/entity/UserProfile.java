package com.activity_hub.notification_feed.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {

    @Id
    @Column(name = "user_id")
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "alias_name", length = 50)
    private String aliasName;

    @Column(name = "is_verified", nullable = false)
    private boolean isVerified = false;

    @Column(name = "cover_image_key")
    private String coverImageKey;

    @Column(name = "avatar_image_key")
    private String avatarImageKey;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "profile_bio_lines", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "bio_line")
    @OrderColumn(name = "line_order")
    private List<String> bioLines;

    private String category;
    private String location;


    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;
}