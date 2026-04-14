package org.example.securecoding.intelbackend.deep.domain;

import org.example.securecoding.intelbackend.deep.infrastructure.JwtValidator;
import java.time.Instant;
import java.util.Objects;

/**
 * DEEP MODEL — Domain Primitive: VerifiedSession
 *
 * This class ensures that a session is only valid if it corresponds to
 * a cryptographically verified token that is bound to the correct user.
 *
 * Immutability and Invariant Enforcement make invalid sessions impossible to represent.
 */
public final class VerifiedSession {
    private final String userId;
    private final String token;
    private final Instant expiresAt;

    public VerifiedSession(String userId, String jwt) {
        this.userId = Objects.requireNonNull(userId, "UserId cannot be null");
        this.token = Objects.requireNonNull(jwt, "Token cannot be null");

        // INVARIANT ENFORCEMENT: Simulate cryptographic JWT validation
        if (!JwtValidator.verify(jwt)) {
            throw new IllegalArgumentException("Invalid JWT signature or format");
        }

        // IDENTITY BINDING: The token must belong to this specific userId
        if (!JwtValidator.extractSub(jwt).equals(userId)) {
            throw new IllegalArgumentException("Session binding failure: Token does not match UserID");
        }

        this.expiresAt = JwtValidator.extractExpiry(jwt);
    }

    public String getUserId() { return userId; }
    public String getToken() { return token; }
    public Instant getExpiresAt() { return expiresAt; }

    // NO SETTERS — Once created, the security state is immutable
}
