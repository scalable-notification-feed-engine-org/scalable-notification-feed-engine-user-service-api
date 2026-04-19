package com.activity_hub.notification_feed.api;

import com.activity_hub.notification_feed.dto.request.LoginRequestDto;
import com.activity_hub.notification_feed.dto.request.PasswordRequestDto;
import com.activity_hub.notification_feed.dto.request.UserRequestDto;
import com.activity_hub.notification_feed.dto.request.UserUpdateRequestDto;
import com.activity_hub.notification_feed.dto.response.UserResponseDto;
import com.activity_hub.notification_feed.service.UserService;
import com.activity_hub.notification_feed.service.impl.JwtService;
import com.activity_hub.notification_feed.util.StandardResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/user-service/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService systemUserService;
    private final JwtService jwtService;

    @PostMapping("/visitors/signup")
    public ResponseEntity<StandardResponseDto> createUser(
                @RequestBody UserRequestDto dto
    ){
        System.out.println("createUser");
        systemUserService.createUser(dto);
        return new ResponseEntity<>(
                new StandardResponseDto(201,"user account was created",null),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/visitors/resend")
    public ResponseEntity<StandardResponseDto> resend(
            @RequestParam String email,
            @RequestParam String type
    ){
        systemUserService.resend(email, type);
        return new ResponseEntity<>(
                new StandardResponseDto(200,"please check you email",null),
                HttpStatus.OK
        );
    }

    @PostMapping("/visitors/forgot-password-request-code")
    public ResponseEntity<StandardResponseDto> forgotPasswordRequest(
            @RequestParam String email
    ){
        systemUserService.forgotPasswordSendVerificationCode(email);
        return new ResponseEntity<>(
                new StandardResponseDto(200,"please check you email",null),
                HttpStatus.OK
        );
    }

    @PostMapping("/visitors/verify-reset")
    public ResponseEntity<StandardResponseDto> verifyReset(
            @RequestParam String email,
            @RequestParam String otp
    ){

        boolean isVerified = systemUserService.verifyReset(otp,email);
        return new ResponseEntity<>(
                new StandardResponseDto(isVerified?200:400,isVerified?"Verified":"try Again",isVerified),
                isVerified?HttpStatus.OK:HttpStatus.BAD_REQUEST
        );
    }

    @PostMapping("/visitors/reset-password")
    public ResponseEntity<StandardResponseDto> resetPassword(
            @RequestBody PasswordRequestDto dto
    ){

        boolean isChanged = systemUserService.passwordReset(dto);
        return new ResponseEntity<>(
                new StandardResponseDto(isChanged?201:400,isChanged?"CHANGED":"try Again",isChanged),
                isChanged?HttpStatus.CREATED:HttpStatus.BAD_REQUEST
        );
    }

    @PostMapping("/visitors/verify-email")
    public ResponseEntity<StandardResponseDto> verifyEmail(
            @RequestParam String email,
            @RequestParam String otp
    ){
        boolean isVerified = systemUserService.verifyEmail(otp,email);
        return new ResponseEntity<>(
                new StandardResponseDto(isVerified?200:400,isVerified?"Verified":"try Again",isVerified),
                isVerified?HttpStatus.OK:HttpStatus.BAD_REQUEST
        );
    }

    @PostMapping("/visitors/login")
    public ResponseEntity<StandardResponseDto> login(
            @RequestBody LoginRequestDto dto
    ){

        return new ResponseEntity<>(
                new StandardResponseDto(200,"success",systemUserService.login(dto)),
                HttpStatus.OK
        );
    }

    @GetMapping("/get-user-details")
    @PreAuthorize("hasAnyRole('CUSTOMER','SUPER_ADMIN')")
    public ResponseEntity<StandardResponseDto> getUserDetails(
            @RequestHeader("Authorization") String tokenHeader
    ) {
        String token = tokenHeader.replace("Bearer ", "");
        String email = jwtService.getEmail(token);

        UserResponseDto userDetails = systemUserService.getUserDetails(email);

        return new ResponseEntity<>(
                new StandardResponseDto(200,
                        "user details!", userDetails),
                HttpStatus.OK
        );
    }


    @GetMapping("/get-all-user-details")
//    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public ResponseEntity<StandardResponseDto> getUserAllDetails(

    ) {
        List<UserResponseDto> userDetails = systemUserService.getAllUsers();

        return new ResponseEntity<>(
                new StandardResponseDto(200,
                        "user details!", userDetails),
                HttpStatus.OK
        );
    }

    @PostMapping("/update-user-details")
    @PreAuthorize("hasAnyRole('CUSTOMER','SUPER_ADMIN')")
    public ResponseEntity<StandardResponseDto> updateUserDetails(
            @RequestHeader("Authorization") String tokenHeader,
            @RequestBody UserUpdateRequestDto dto
    ) {
        String token = tokenHeader.replace("Bearer ", "");
        String email = jwtService.getEmail(token);

        systemUserService.updateUserDetails(email,dto);

        return new ResponseEntity<>(
                new StandardResponseDto(201,
                        "user details updated!", null),
                HttpStatus.CREATED
        );
    }

}
