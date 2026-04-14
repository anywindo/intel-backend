package org.example.securecoding.intelbackend.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * DEEP MODEL PLACEHOLDER: VerifiedAuthSession
 * 
 * Secure by Design Principle:
 * Prevents Boolean Auth Bypass. Replaces the mutable `isAuthenticated` boolean
 * and raw `String[] roles` with an immutable, cryptographically verified session state.
 */
public final class VerifiedAuthSession {
    private final String userId;
    private final Role role;
    private final Instant expiresAt;

    private VerifiedAuthSession(String userId, Role role, Instant expiresAt) {
        Objects.requireNonNull(userId);
        Objects.requireNonNull(role);
        if (expiresAt.isBefore(Instant.now())) throw new IllegalArgumentException("Expired");
        this.userId = userId;
        this.role = role;
        this.expiresAt = expiresAt;
    }

    // Factory method: satu-satunya cara membuat session valid
    public static VerifiedAuthSession fromValidatedToken(String jwt) {
        org.example.securecoding.intelbackend.util.JwtValidator.Claims claims = org.example.securecoding.intelbackend.util.JwtValidator.verify(jwt);
        return new VerifiedAuthSession(
            claims.getSubject(),
            Role.fromServerToken(claims.get("role", String.class)),
            claims.getExpiration()
        );
    }

    public String getUserId() {
        return userId;
    }

    public Role getRole() {
        return role;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
