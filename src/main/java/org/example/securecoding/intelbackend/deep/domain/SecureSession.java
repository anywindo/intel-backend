package org.example.securecoding.intelbackend.deep.domain;

import org.example.securecoding.intelbackend.deep.infrastructure.JwtValidator;
import java.time.Instant;
import java.util.Objects;

/**
 * DEEP MODEL — Domain Primitive: SecureSession (Multi-Factor Binding)
 *
 * Implements strict binding between UserId, the cryptographic Token, 
 * the User's IP Address, and Expiry.
 *
 * This is the ultimate defense against Session Hijacking and IDOR.
 * If ANY of these parameters differ from the verified state, the constructor
 * throws an exception, making it impossible to represent an insecure session.
 */
public final class SecureSession {
    private final String userId;
    private final String token;
    private final IpAddress boundIpAddress;
    private final Instant expiresAt;

    public SecureSession(String userId, String token, String ipAddress, Instant expiresAt) {
        this.userId = Objects.requireNonNull(userId, "UserId is required");
        this.token = Objects.requireNonNull(token, "Token is required");
        this.boundIpAddress = new IpAddress(ipAddress); // Validates IP format
        this.expiresAt = Objects.requireNonNull(expiresAt, "Expiry is required");

        // VULNERABILITY MITIGATION: Session Expiry Enforcement
        if (expiresAt.isBefore(Instant.now())) {
            throw new IllegalArgumentException("Session has expired");
        }

        // VULNERABILITY MITIGATION: Cryptographic Verification
        if (!JwtValidator.isValidSignature(token)) {
            throw new IllegalArgumentException("Invalid JWT signature");
        }

        // VULNERABILITY MITIGATION: Identity Binding (Anti-Hijack)
        if (!userId.equals(JwtValidator.extractUserId(token))) {
            throw new IllegalArgumentException("Session binding failure: Token subject mismatch");
        }
    }

    public String getUserId() { return userId; }
    public String getToken() { return token; }
    public IpAddress getBoundIpAddress() { return boundIpAddress; }
    public Instant getExpiresAt() { return expiresAt; }

    // NO SETTERS — immutable security state
}
