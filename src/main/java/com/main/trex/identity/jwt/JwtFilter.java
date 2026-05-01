package com.main.trex.identity.jwt;

import com.main.trex.identity.entity.UserType;
import com.main.trex.identity.security.auth.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        logger.debug("JwtFilter called for URI: {}", request.getRequestURI());

        try {
            String jwt = jwtUtils.getJwtFromHeader(request);

            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {

                // 1. block unverified users from accessing protected endpoints
                Boolean isEmailVerified = jwtUtils.extractIsEmailVerified(jwt);
                if (Boolean.FALSE.equals(isEmailVerified)) {
                    logger.warn("Blocked unverified user attempting to access: {}", request.getRequestURI());
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Email not verified");
                    return;
                }

                // 2. load user by email (subject)
                String email = jwtUtils.getUserNameFromJwtToken(jwt);
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                // 3. set authentication in security context
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // 4. expose context info as request attributes for use in controllers
                UserType activeContext = jwtUtils.extractActiveContext(jwt);
                Long orgId = jwtUtils.extractOrgId(jwt);
                Long userId = jwtUtils.extractUserId(jwt);

                request.setAttribute("activeContext", activeContext);
                request.setAttribute("orgId", orgId);
                request.setAttribute("userId", userId);

                logger.debug("Authenticated user: {}, context: {}, orgId: {}", email, activeContext, orgId);
            }

        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}