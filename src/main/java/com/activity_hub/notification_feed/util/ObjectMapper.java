package com.activity_hub.notification_feed.util;

import com.activity_hub.notification_feed.dto.event.UserSendOtpEvent;
import com.activity_hub.notification_feed.dto.request.UserRequestDto;
import com.activity_hub.notification_feed.dto.response.UserResponseDto;
import com.activity_hub.notification_feed.entity.User;
import com.activity_hub.notification_feed.enums.UserStatus;
import com.activity_hub.notification_feed.exception.UnAuthorizedException;
import com.activity_hub.notification_feed.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ObjectMapper {

    private final UserRepository userRepository;

    public UserRepresentation mapUserRepo(UserRequestDto dto, boolean isEmailVerified, boolean isEnable) {
      if(dto == null) return null;
      UserRepresentation user = new UserRepresentation();
      user.setEmail(dto.getEmail());
      user.setFirstName(dto.getFirstName());
      user.setLastName(dto.getLastName());
      user.setEnabled(isEnable);
      user.setEmailVerified(isEmailVerified);
      List<CredentialRepresentation> credentialList = new ArrayList<>();
      CredentialRepresentation credential = new CredentialRepresentation();
      credential.setTemporary(false);
      credential.setValue(dto.getPassword());
      credentialList.add(credential);
      user.setCredentials(credentialList);
      return user;
    }

    public UserSendOtpEvent toCreateEvent(User user, String otp) {
        return UserSendOtpEvent.builder()
                .user_id(user.getId().toString())
                .email(user.getEmail())
                .otp(otp)
                .first_name(user.getFirstName())
                .last_name(user.getLastName())
                .build();
    }

    public List<String> extractRoles(Map<String, Object> tokenData) {
        Map<String, Object> realmAccess = (Map<String, Object>) tokenData.get("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            return (List<String>) realmAccess.get("roles");
        }
        return List.of();
    }

    public User validateRegularUserLogin(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found. Please register first."));

        if (!user.isEmailVerified()) {
            throw new UnAuthorizedException("Please verify your email before logging in");
        }


        if (user.getStatus() == UserStatus.BLOCKED || user.getStatus() == UserStatus.SUSPENDED) {
            throw new UnAuthorizedException("Your account has been " + user.getStatus());
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalStateException("Account is not active");
        }

        return user;
    }

    public UserResponseDto mapToUserResponse(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .contact(user.getContact())
                .role(user.getRole())
                .emailVerified(user.isEmailVerified())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
