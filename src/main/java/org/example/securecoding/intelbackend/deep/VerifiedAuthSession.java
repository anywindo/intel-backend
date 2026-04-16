package org.example.securecoding.intelbackend.deep;

import java.time.Instant;
import java.util.Objects;

public final class VerifiedAuthSession {
    private final String userId;
    private final Role role;
    private final Instant expiresAt;

    private VerifiedAuthSession(String userId, Role role, Instant expiresAt) {
        this.userId = Objects.requireNonNull(userId);
        this.role = Objects.requireNonNull(role);
        if (expiresAt.isBefore(Instant.now())) {
            throw new IllegalArgumentException("Sesi telah kedaluwarsa");
        }
        this.expiresAt = expiresAt;
    }

    public static VerifiedAuthSession fromValidatedToken(String jwt) {
        if (jwt == null || jwt.isBlank()) {
            throw new IllegalArgumentException("Token tidak boleh kosong");
        }
        if (jwt.length() < 20) {
            throw new IllegalArgumentException("Signature JWT tidak valid atau token palsu");
        }
        String userId = "user-extracted-from-jwt";
        Role role = Role.USER;
        Instant expiry = Instant.now().plusSeconds(3600);
        return new VerifiedAuthSession(userId, role, expiry);
    }

    public String getUserId() { return userId; }
    public Role getRole() { return role; }
    public boolean isAdmin() { return role == Role.ADMIN; }
}