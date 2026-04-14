package org.example.securecoding.intelbackend.deep.domain;

import org.example.securecoding.intelbackend.deep.infrastructure.JwtValidator;
import java.time.Instant;
import java.util.Objects;

/**
 * DEEP MODEL — Domain Primitive: VerifiedAuthSession
 *
 * Adds Role information to the verified session.
 * This is used for role-based access control (RBAC) in Case B.
 * Implements 'Make Invalid State Unrepresentable'.
 */
public final class VerifiedAuthSession {
    private final String userId;
    private final Role role;
    private final Instant expiresAt;

    private VerifiedAuthSession(String userId, Role role, Instant expiresAt) {
        this.userId = Objects.requireNonNull(userId, "UserId is required");
        this.role = Objects.requireNonNull(role, "Role is required");
        this.expiresAt = Objects.requireNonNull(expiresAt, "Expiry is required");
        
        if (expiresAt.isBefore(Instant.now())) {
            throw new IllegalArgumentException("Session has expired");
        }
    }

    /**
     * Factory Method: The ONLY way to create a valid session.
     * It enforces verification during construction, ensuring that only
     * cryptographically verified tokens can enter the system.
     */
    public static VerifiedAuthSession fromValidatedToken(String jwt) {
        if (!JwtValidator.verify(jwt)) {
            throw new IllegalArgumentException("Invalid JWT signature or expired token");
        }

        String userId = JwtValidator.extractSub(jwt);
        Role role = Role.fromString(JwtValidator.extractRole(jwt));
        Instant expiry = JwtValidator.extractExpiry(jwt);

        return new VerifiedAuthSession(userId, role, expiry);
    }

    public String getUserId() { return userId; }
    public Role getRole() { return role; }
    public boolean isAdmin() { return role == Role.ADMIN; }
    public Instant getExpiresAt() { return expiresAt; }

    // NO SETTERS — immutable core security state
}
