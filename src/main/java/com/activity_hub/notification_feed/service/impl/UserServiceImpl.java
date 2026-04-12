package com.activity_hub.notification_feed.service.impl;

import com.activity_hub.notification_feed.config.KeycloakConfig;
import com.activity_hub.notification_feed.dto.request.LoginRequestDto;
import com.activity_hub.notification_feed.dto.request.PasswordRequestDto;
import com.activity_hub.notification_feed.dto.request.UserRequestDto;
import com.activity_hub.notification_feed.dto.request.UserUpdateRequestDto;
import com.activity_hub.notification_feed.dto.response.LoginResponseDto;
import com.activity_hub.notification_feed.dto.response.UserResponseDto;
import com.activity_hub.notification_feed.entity.User;
import com.activity_hub.notification_feed.enums.UserRole;
import com.activity_hub.notification_feed.enums.UserStatus;
import com.activity_hub.notification_feed.event.EventPublisher;
import com.activity_hub.notification_feed.exception.DuplicateEntryException;
import com.activity_hub.notification_feed.repository.UserRepository;
import com.activity_hub.notification_feed.service.UserService;
import com.activity_hub.notification_feed.util.ObjectMapper;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    @Value("${keycloak.realm}")
    private String realm;
    private final KeycloakConfig keycloakConfig;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final EventPublisher eventPublisher;
    private final RedisService redisService;

    @Override
    public void createUser(UserRequestDto dto) {
        String userId;
        Keycloak keycloak;
        UserRepresentation existingUser;

        keycloak = keycloakConfig.keycloak();

        existingUser = keycloak.realm(realm).users().search(dto.getEmail()).stream()
                .findFirst().orElse(null);

        if (existingUser != null) {
            Optional<User> selectedUserFromUserService = userRepository.findByEmail(dto.getEmail());

            if (selectedUserFromUserService.isEmpty()) {
                keycloak.realm(realm).users().delete(dto.getEmail());
            } else {
                throw new DuplicateEntryException("Email already exists");
            }
        }

        UserRepresentation userRepresentation = objectMapper.mapUserRepo(dto, false, false);
        Response response = keycloak.realm(realm).users().create(userRepresentation);
        if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
            RoleRepresentation userRole = keycloak.realm(realm).roles().get(UserRole.USER.toString()).toRepresentation();
            userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
            keycloak.realm(realm).users().get(userId).roles().realmLevel().add(Collections.singletonList(userRole));
            UserRepresentation createUser = keycloak.realm(realm).users().get(userId).toRepresentation();

            User user = User.builder()
                    .id(UUID.randomUUID())
                    .keycloakId(createUser.getId())
                    .email(dto.getEmail())
                    .firstName(dto.getFirstName())
                    .lastName(dto.getLastName())
                    .contact(dto.getContact())
                    .status(UserStatus.PENDING)
                    .role(UserRole.USER)
                    .isActive(false)
                    .isAccountNonExpired(true)
                    .isAccountNonLocked(true)
                    .isCredentialsNonExpired(true)
                    .isEnabled(false)
                    .isEmailVerified(false)
                    .build();

            User savedUser = userRepository.save(user);

            try{
                eventPublisher
                        .publishUserSendOtp(objectMapper.toCreateEvent(savedUser,redisService.saveOtp(savedUser.getEmail())));
            }catch (Exception e){
                log.error("Failed to publish user created event", e);

            }
        }
    }

    @Override
    public LoginResponseDto login(LoginRequestDto dto) {
        return null;
    }

    @Override
    public void resend(String email, String type) {

    }

    @Override
    public void forgotPasswordSendVerificationCode(String email) {

    }

    @Override
    public boolean verifyReset(String otp, String email) {
        return false;
    }

    @Override
    public boolean passwordReset(PasswordRequestDto dto) {
        return false;
    }

    @Override
    public boolean verifyEmail(String otp, String email) {
        return false;
    }

    @Override
    public void updateUserDetails(String email, UserUpdateRequestDto data) {

    }

    @Override
    public UserResponseDto getUserDetails(String email) {
        return null;
    }
}
