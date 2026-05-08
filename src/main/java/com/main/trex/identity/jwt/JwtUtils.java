package com.main.trex.identity.jwt;

import com.main.trex.identity.entity.BusinessUser;
import com.main.trex.identity.entity.User;
import com.main.trex.identity.entity.UserType;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${spring.app.jwtSecret}")
    private String jwtSecret;

    @Value("${spring.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    public String getJwtFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        logger.debug("Authorization Header: {}", bearerToken);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // ----------------------------------------------------------------
    // Generate token from UserDetails only (fallback / simple use)
    // keeps backward compatibility with your existing code
    // ----------------------------------------------------------------
    public String generateTokenFromUsername(UserDetails userDetails) {
        String username = userDetails.getUsername();
        String roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key())
                .compact();
    }

    public String generateToken(User user) {
        JwtBuilder builder = Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("userType", user.getUserType().name())
                .claim("activeContext", user.getActiveContext().name())
                .claim("isEmailVerified", user.getIsEmailVerified());

        if (user.getActiveContext() == UserType.BUSINESS
                && user.getBusinessProfile() != null) {

            BusinessUser business = user.getBusinessProfile();
            builder.claim("orgId", business.getOrganization().getId());
            builder.claim("role", business.getUser().getRoles());

        } else {
            builder.claim("orgId", null);
            builder.claim("role", "ROLE_USER");
        }

        return builder
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key())
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return getClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        return getClaims(token).get("userId", Long.class);
    }

    public UserType extractUserType(String token) {
        return UserType.valueOf(getClaims(token).get("userType", String.class));
    }

    public UserType extractActiveContext(String token) {
        return UserType.valueOf(getClaims(token).get("activeContext", String.class));
    }

    public Long extractOrgId(String token) {
        return getClaims(token).get("orgId", Long.class); // null for personal context
    }

    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    public Boolean extractIsEmailVerified(String token) {
        return getClaims(token).get("isEmailVerified", Boolean.class);
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().verifyWith((SecretKey) key()).build().parseSignedClaims(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }
}