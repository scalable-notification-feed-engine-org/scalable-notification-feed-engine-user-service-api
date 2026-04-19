package com.activity_hub.notification_feed.repository;

import com.activity_hub.notification_feed.dto.response.UserMetadataResponseDTO;
import com.activity_hub.notification_feed.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(@NotBlank(message = "Email is required") @Email(message = "Email format is not valid") String email);

    @Query("SELECT new com.activity_hub.notification_feed.dto.response.UserMetadataResponseDTO(" +
            "u.id, u.firstName, u.lastName, a.resourceUrl) " +
            "FROM User u LEFT JOIN UserAvatar a ON u.id = a.user.id " +
            "WHERE u.id IN :ids")
    List<UserMetadataResponseDTO> findAllMetadataByIds(@Param("ids") List<UUID> ids);

    @Query(value = "SELECT * FROM users", nativeQuery = true)
    List<User> findAllUsers();

}
