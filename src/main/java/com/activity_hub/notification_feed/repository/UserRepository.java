package com.activity_hub.notification_feed.repository;

import com.activity_hub.notification_feed.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(@NotBlank(message = "Email is required") @Email(message = "Email format is not valid") String email);
}
