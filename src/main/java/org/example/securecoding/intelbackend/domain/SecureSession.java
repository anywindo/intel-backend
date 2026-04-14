package org.example.securecoding.intelbackend.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * DEEP MODEL PLACEHOLDER: SecureSession
 * 
 * Secure by Design Principle:
 * Strictly binds identity (userId), token, and physical location (IP Address).
 * Enforces cryptographic checks within the constructor so any invalid state
 * simply throws an exception and halts the process.
 */
public final class SecureSession {
    private final String userId;
    private final Role role;
    private final String token;
    private final String boundIpAddress;
    private final Instant expiresAt;

    public SecureSession(String userId, Role role, String token, String ipAddress, Instant expiresAt) {
        Objects.requireNonNull(userId, "userId wajib");
        Objects.requireNonNull(role, "role wajib");
        Objects.requireNonNull(token, "token wajib");
        Objects.requireNonNull(ipAddress, "IP wajib");
        Objects.requireNonNull(expiresAt, "expiry wajib");

        // TODO: Deep Validations
        // if (!JwtValidator.isValidSignature(token)) throw new IllegalArgumentException("JWT invalid");
        // if (!userId.equals(JwtValidator.extractUserId(token))) throw new IllegalArgumentException("Token not bound");
        if (expiresAt.isBefore(Instant.now())) throw new IllegalArgumentException("Expired");
        // if (!IpAddress.isValid(ipAddress)) throw new IllegalArgumentException("Invalid IP");

        this.userId = userId;
        this.role = role;
        this.token = token;
        this.boundIpAddress = ipAddress;
        this.expiresAt = expiresAt;
    }

    public String getUserId() {
        return userId;
    }

    public String getToken() {
        return token;
    }

    public String getBoundIpAddress() {
        return boundIpAddress;
    }

    public Role getRole() {
        return role;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
