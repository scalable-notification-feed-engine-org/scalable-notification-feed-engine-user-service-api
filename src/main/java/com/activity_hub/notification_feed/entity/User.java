package com.activity_hub.notification_feed.entity;

import com.activity_hub.notification_feed.enums.UserRole;
import com.activity_hub.notification_feed.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "keycloak_user_id", unique = true, nullable = false)
    private String keycloakId;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status;

    @Column(nullable = false)
    private boolean isActive;

    @Column(nullable = false)
    private boolean isAccountNonExpired;

    @Column(nullable = false)
    private boolean isAccountNonLocked;

    @Column(nullable = false)
    private boolean isCredentialsNonExpired;

    @Column(nullable = false)
    private boolean isEnabled;

    @Column(nullable = false)
    private boolean isEmailVerified;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "contact", length = 20)
    private String contact;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "id",
            referencedColumnName = "user_id",
            insertable = false,
            updatable = false
    )
    private UserStats stats;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}