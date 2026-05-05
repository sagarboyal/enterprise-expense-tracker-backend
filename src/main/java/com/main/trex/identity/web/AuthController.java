package com.main.trex.identity.web;

import com.main.trex.identity.payload.request.UserRequest;
import com.main.trex.identity.entity.User;
import com.main.trex.identity.jwt.JwtUtils;
import com.main.trex.identity.payload.request.SignInRequest;
import com.main.trex.identity.repository.UserRepository;
import com.main.trex.shared.exception.ApiException;
import com.main.trex.shared.payload.response.MessageResponse;
import com.main.trex.identity.payload.response.SignInResponse;
import com.main.trex.identity.payload.response.UserInfoResponse;
import com.main.trex.identity.service.UserService;
import com.main.trex.identity.util.AuthUtils;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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
    private final UserRepository userRepository;

    @PostMapping("/public/sign-in")
    public ResponseEntity<?> authenticateUser(@RequestBody SignInRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (AuthenticationException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "message", "Bad credentials",
                            "error", e.getMessage(),
                            "status", false
                    ));
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiException("User not found"));
        String token = jwtUtils.generateToken(user);

        SignInResponse response = SignInResponse.builder()
                .activeContext(user.getActiveContext())
                .token(token)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/public/sign-up")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRequest request) {
        userService.createUser(request);
        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @GetMapping("/public/oauth2/google")
    public ResponseEntity<Map<String, String>> getGoogleLoginUrl(HttpServletRequest request) {
        String authorizationUrl = ServletUriComponentsBuilder.fromContextPath(request)
                .path("/oauth2/authorization/google")
                .build()
                .toUriString();

        return ResponseEntity.ok(Map.of("authorizationUrl", authorizationUrl));
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


