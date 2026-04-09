package com.main.trex.identity.security.oauth2;

import com.main.trex.identity.entity.AuthProvider;
import com.main.trex.identity.entity.Role;
import com.main.trex.identity.entity.Roles;
import com.main.trex.identity.entity.User;
import com.main.trex.identity.jwt.JwtUtils;
import com.main.trex.identity.repository.RoleRepository;
import com.main.trex.identity.repository.UserRepository;
import com.main.trex.identity.security.auth.CustomUserDetails;
import com.main.trex.notification.entity.Notification;
import com.main.trex.notification.service.NotificationService;
import com.main.trex.shared.exception.ApiException;
import com.main.trex.shared.exception.ResourceNotFoundException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final NotificationService notificationService;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Value("${oauth2.authorizedRedirectPath}")
    private String authorizedRedirectPath;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String providerId = oauth2User.getName();

        if (email == null || email.isBlank()) {
            throw new ApiException("Google account did not provide an email address.");
        }

        User user = userRepository.findByEmail(email)
                .map(existingUser -> syncOAuthUser(existingUser, name, providerId))
                .orElseGet(() -> createOAuthUser(email, name, providerId));

        String token = jwtUtils.generateTokenFromUsername(CustomUserDetails.build(user));
        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + authorizedRedirectPath)
                .queryParam("token", token)
                .queryParam("username", user.getEmail())
                .build()
                .toUriString();

        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private User createOAuthUser(String email, String name, String providerId) {
        Role employeeRole = roleRepository.findByRoleName(Roles.ROLE_EMPLOYEE)
                .orElseThrow(() -> new ResourceNotFoundException("Default employee role not found."));

        User user = new User();
        user.setEmail(email);
        user.setFullName(name != null && !name.isBlank() ? name : email);
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setProvider(AuthProvider.GOOGLE);
        user.setProviderId(providerId);
        user.setRoles(Set.of(employeeRole));
        user = userRepository.save(user);

        notificationService.saveNotification(
                new Notification("Your Google account has been linked and your personal workspace is ready."),
                user.getId()
        );

        return user;
    }

    private User syncOAuthUser(User user, String name, String providerId) {
        if (user.getProvider() == null || user.getProvider() == AuthProvider.LOCAL) {
            user.setProvider(AuthProvider.GOOGLE);
        }
        user.setProviderId(providerId);
        if (name != null && !name.isBlank()) {
            user.setFullName(name);
        }
        return userRepository.save(user);
    }
}
