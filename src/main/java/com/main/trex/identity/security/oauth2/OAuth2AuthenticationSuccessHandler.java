package com.main.trex.identity.security.oauth2;

import com.main.trex.identity.entity.User;
import com.main.trex.identity.jwt.JwtUtils;
import com.main.trex.identity.service.impl.UserRegistrationService;
import com.main.trex.shared.exception.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRegistrationService userRegistrationService;
    private final JwtUtils jwtUtils;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Value("${oauth2.authorizedRedirectPath}")
    private String authorizedRedirectPath;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        try {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            String email = oauth2User.getAttribute("email");
            String name = oauth2User.getAttribute("name");
            String providerId = oauth2User.getName();

            if (email == null || email.isBlank()) {
                redirectWithError(request, response, "email_not_provided");
                return;
            }

            User user = userRegistrationService.findOrCreateOAuthUser(email, name, providerId);

            String token = jwtUtils.generateToken(user);
            String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + authorizedRedirectPath)
                    .queryParam("token", token)
                    .queryParam("username", user.getEmail())
                    .build()
                    .toUriString();

            clearAuthenticationAttributes(request);
            getRedirectStrategy().sendRedirect(request, response, targetUrl);

        } catch (ApiException ex) {
            log.error("OAuth2 business error: {}", ex.getMessage());
            redirectWithError(request, response, "provider_mismatch");
        } catch (Exception ex) {
            log.error("OAuth2 unexpected error", ex);
            redirectWithError(request, response, "oauth2_auth_failed");
        }
    }

    private void redirectWithError(HttpServletRequest request,
                                   HttpServletResponse response,
                                   String errorCode) throws IOException {
        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/login")
                .queryParam("error", errorCode)
                .build()
                .toUriString();
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
