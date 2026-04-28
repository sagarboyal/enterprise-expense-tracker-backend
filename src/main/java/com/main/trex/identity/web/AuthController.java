package com.main.trex.identity.web;

import com.main.trex.identity.payload.request.UserRequest;
import com.main.trex.identity.entity.User;
import com.main.trex.identity.jwt.JwtUtils;
import com.main.trex.identity.payload.request.SignInRequest;
import com.main.trex.shared.payload.response.MessageResponse;
import com.main.trex.identity.payload.response.SignInResponse;
import com.main.trex.identity.payload.response.UserInfoResponse;
import com.main.trex.notification.service.NotificationService;
import com.main.trex.identity.service.UserService;
import com.main.trex.identity.util.AuthUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final AuthUtils authUtils;
    private final UserService userService;
    private final NotificationService notificationService;

    @PostMapping("/public/sign-in")
    public ResponseEntity<?> authenticateUser(@RequestBody SignInRequest loginRequest) {
        Authentication authentication;
        try {
            authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        } catch (AuthenticationException exception) {
            Map<String, Object> map = new HashMap<>();
            map.put("message", "Bad credentials");
            map.put("error", exception.getMessage());
            map.put("status", false);
            return new ResponseEntity<Object>(map, HttpStatus.NOT_FOUND);
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String jwtToken = jwtUtils.generateTokenFromUsername(userDetails);


        return ResponseEntity.ok(SignInResponse.builder()
                .username(userDetails.getUsername())
                .token(jwtToken)
                .build());
    }

    @PostMapping("/public/sign-up")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRequest request) {
        userService.createUser(request);
        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @GetMapping("/public/oauth2/google")
    public ResponseEntity<Map<String, String>> getGoogleLoginUrl() {
        return ResponseEntity.ok(Map.of("authorizationUrl", "/oauth2/authorization/google"));
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUserDetails(@AuthenticationPrincipal UserDetails userDetails) {
        User user = authUtils.loggedInUser();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        UserInfoResponse response = new UserInfoResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                roles
        );

        return ResponseEntity.ok().body(response);
    }
    @PostMapping("/public/forgot-password")
    public ResponseEntity<?> forgetPassword(@RequestParam String email) {
        try {
            userService.generatePasswordResetToken(email);
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Invalid email!"));
        }
        return ResponseEntity.ok(new MessageResponse("Password reset token generated successfully!"));
    }

    @PostMapping("/public/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String token,
                                           @RequestParam String newPassword) {
        try {
            userService.resetPassword(token, newPassword);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Invalid token!"));
        }
        return ResponseEntity.ok(new MessageResponse("Password reset successfully!"));
    }
}


