package org.example.securecoding.intelbackend.domain;

import org.example.securecoding.intelbackend.util.JwtValidator;

import java.time.Instant;
import java.util.Objects;

/**
 * DEEP MODEL PLACEHOLDER: VerifiedSession
 * 
 * Secure by Design Principle:
 * This domain primitive encapsulates a cryptographically verified user session.
 * It strictly binds the user's identity to a verified JWT token.
 * Since it only has a constructor with validation logic and no setters (Immutable),
 * an invalid or expired session state becomes unrepresentable in the domain.
 */
public final class VerifiedSession {
    private final String userId;
    private final Role role;
    private final String token;
    private final Instant expiresAt;

    public VerifiedSession(String userId, String jwt) {
        Objects.requireNonNull(userId);
        Objects.requireNonNull(jwt);
        
        JwtValidator.Claims claims = JwtValidator.verify(jwt);
        if (!userId.equals(claims.getSubject())) {
            throw new IllegalArgumentException("Token belongs to a different subject");
        }
        
        this.userId = userId;
        this.role = Role.fromServerToken(claims.getRole());
        this.token = jwt;
        this.expiresAt = claims.getExpiration();
    }

    public String getUserId() {
        return userId;
    }

    public Role getRole() {
        return role;
    }

    public String getToken() {
        return token;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
