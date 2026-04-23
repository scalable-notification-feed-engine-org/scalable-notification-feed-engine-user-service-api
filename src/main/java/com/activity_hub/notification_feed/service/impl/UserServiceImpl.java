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
import com.activity_hub.notification_feed.exception.BadRequestException;
import com.activity_hub.notification_feed.exception.DuplicateEntryException;
import com.activity_hub.notification_feed.exception.KeycloakException;
import com.activity_hub.notification_feed.exception.NotFoundException;
import com.activity_hub.notification_feed.repository.UserRepository;
import com.activity_hub.notification_feed.service.UserService;
import com.activity_hub.notification_feed.util.ObjectMapper;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.springframework.http.*;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    @Value("${keycloak.realm}")
    private String realm;
    @Value("${keycloak.client-id}")
    private String clientId;
    @Value("${keycloak.client-secret}")
    private String clientSecret;
    @Value("${spring.security.oauth2.resourceserver.jwt.token-uri}")
    private String keycloakApiTokenUri;

    private final KeycloakConfig keycloakConfig;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final EventPublisher eventPublisher;
    private final RedisService redisService;


    @Override
    @Transactional
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

         try{

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

             TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                 @Override
                 public void afterCommit() {
                     try {
                         String otp = redisService
                                 .saveOtp(savedUser.getEmail());
                         System.out.println("OTP : " + otp);
                         eventPublisher
                                 .publishUserSendOtp(objectMapper.toCreateEvent(savedUser,otp));
                     }catch (Exception e){
                        log.error("Failed to publish user created event", e);
                     }
                 }
             });

            }catch (Exception e){
             keycloakConfig.keycloak().realm(realm).users().delete(userId);
             throw new BadRequestException("Error while saving user");
            }
        }
    }

    @Override
    public LoginResponseDto login(LoginRequestDto dto) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("client_id", clientId);
        requestBody.add("client_secret", clientSecret);
        requestBody.add("grant_type", OAuth2Constants.PASSWORD);
        requestBody.add("username", dto.getEmail());
        requestBody.add("password", dto.getPassword());

        RestTemplate restTemplate = new RestTemplate();

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    keycloakApiTokenUri,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            Map<String,Object> tokenResponse = response.getBody();

            LoginResponseDto loginResponse = LoginResponseDto.builder()
                    .accessToken(String.valueOf(tokenResponse.get("access_token")))
                    .refreshToken(String.valueOf(tokenResponse.get("refresh_token")))
                    .expiresIn(((Number) tokenResponse.get("expires_in")).longValue())
                    .tokenType((String) tokenResponse.get("token_type"))
                    .build();

            User user = objectMapper.validateRegularUserLogin(dto.getEmail());
            loginResponse.setUser(objectMapper.mapToUserResponse(user));

            return  loginResponse;

        }catch (Exception ex){
            throw new KeycloakException("Login failed: " + ex.getMessage(), ex);
        }

    }

    @Override
    public void resend(String email, String type) {
        User selectedUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Email not found"));

        if(type.equalsIgnoreCase("SIGNUP")){
            if(selectedUser.isEmailVerified()){
                throw new DuplicateEntryException("The email is already activated");
            }
        }

        try {
            String otp = redisService.saveOtp(selectedUser.getEmail());

            eventPublisher
                    .publishUserSendOtp(objectMapper.toCreateEvent(selectedUser,otp));


        } catch (ExecutionException | InterruptedException e) {

            throw new RuntimeException(e.getMessage());
        }

    }

    @Override
    public void forgotPasswordSendVerificationCode(String email) {
        User selectedUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Email not found"));
        try {
        Keycloak keycloak = keycloakConfig.keycloak();
        keycloak
                .realm(realm).users().search(email).stream().findFirst()
                .orElseThrow(() -> new NotFoundException("Email not found"));


            String otp = redisService.saveOtp(selectedUser.getEmail());

            eventPublisher
                    .publishUserSendOtp(objectMapper.toCreateEvent(selectedUser,otp));


        } catch (ExecutionException | InterruptedException e) {

            throw new RuntimeException(e.getMessage());
        }

    }

    @Override
    public boolean verifyReset(String otp, String email) {
        User selectedUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Email not found"));

        String otpValue = redisService.getValue(selectedUser.getEmail());

        if(otpValue.equals(otp)){
            redisService.verifyAndDeleteOtp(selectedUser.getEmail(), otp);
            return true;
        }

        return false;

    }

    @Override
    public boolean passwordReset(PasswordRequestDto dto) {
        User selectedUser = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new NotFoundException("Email not found"));

        Keycloak keycloak = keycloakConfig.keycloak();
        List<UserRepresentation> keycloakUsers = keycloak.realm(realm).users().search(selectedUser.getEmail());

        if(!keycloakUsers.isEmpty() && dto.getCode().equals(redisService.getValue(selectedUser.getEmail()))){
            UserRepresentation keycloakUser = keycloakUsers.get(0);
            UserResource userResource = keycloak.realm(realm).users().get(keycloakUser.getId());
            CredentialRepresentation newPassword = new CredentialRepresentation();
            newPassword.setType(CredentialRepresentation.PASSWORD);
            newPassword.setTemporary(false);
            userResource.resetPassword(newPassword);

            userRepository.save(selectedUser);
            redisService.verifyAndDeleteOtp(selectedUser.getEmail(), dto.getCode());
            return true;
        }
        throw new BadRequestException("try again");
    }

    @Override
    public boolean verifyEmail(String otp, String email) {
        User selectedUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Email not found"));

        String otpValue = redisService.getValue(selectedUser.getEmail());

        if(otpValue.equals(otp)){
            UserRepresentation keycloakUser = keycloakConfig.keycloak().realm(realm).users().search(email).stream().findFirst()
                    .orElseThrow(() -> new NotFoundException("Email not found"));
            keycloakUser.setEmailVerified(true);
            keycloakUser.setEnabled(true);

            keycloakConfig.keycloak()
                    .realm(realm).users().get(keycloakUser.getId()).update(keycloakUser);

            selectedUser.setEmailVerified(true);
            selectedUser.setEnabled(true);
            selectedUser.setStatus(UserStatus.ACTIVE);
            selectedUser.setActive(true);

            userRepository.save(selectedUser);
            redisService.verifyAndDeleteOtp(selectedUser.getEmail(), otp);
            return true;
        }

        return false;
    }

    @Override
    public void updateUserDetails(String email, UserUpdateRequestDto data) {
        Optional<User> byEmail = userRepository.findByEmail(email);
        if (byEmail.isEmpty()) {
            throw new NotFoundException("User was not found");
        }

        User systemUser = byEmail.get();
        Keycloak keycloak = keycloakConfig.keycloak();
        List<UserRepresentation> keyCloakUsers = keycloak.realm(realm).users().search(systemUser.getEmail());
        if (!keyCloakUsers.isEmpty()) {
            UserRepresentation keyCloakUser = keyCloakUsers.get(0);
            keyCloakUser.setFirstName(data.getFirstName());
            keyCloakUser.setLastName(data.getLastName());
            byEmail.get().setFirstName(data.getFirstName());
            byEmail.get().setLastName(data.getLastName());
            userRepository.save(systemUser);
        }
    }

    @Override
    public UserResponseDto getUserDetails(String email) {
        Optional<User> byEmail = userRepository.findByEmail(email);
        if (byEmail.isEmpty()) {
            throw new NotFoundException("User was not found");
        }

        return UserResponseDto.builder()
                .id(byEmail.get().getId())
                .email(byEmail.get().getEmail())
                .firstName(byEmail.get().getFirstName())
                .lastName(byEmail.get().getLastName())
                .build();
    }

    @Override
    public List<UserResponseDto> getAllUsers() {
        List<User> allUsers = userRepository.findAllUsers();
        return allUsers.stream().map(objectMapper::mapToUserResponse).toList();
    }


}
