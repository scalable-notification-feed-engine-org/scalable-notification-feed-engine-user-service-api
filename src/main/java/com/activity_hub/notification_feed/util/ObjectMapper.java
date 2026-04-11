package com.activity_hub.notification_feed.util;

import com.activity_hub.notification_feed.dto.event.UserSendOtpEvent;
import com.activity_hub.notification_feed.dto.request.UserRequestDto;
import com.activity_hub.notification_feed.entity.User;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ObjectMapper {
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
}
