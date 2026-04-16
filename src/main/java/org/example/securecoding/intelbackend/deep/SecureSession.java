package org.example.securecoding.intelbackend.deep;

import java.time.Instant;

public final class SecureSession {
    private final String userId;
    private final String token;
    private final Instant expiresAt;

    public SecureSession(String userId, String token, Instant expiresAt) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("UserId tidak boleh kosong");
        }
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token tidak boleh kosong");
        }
        if (expiresAt.isBefore(Instant.now())) {
            throw new IllegalArgumentException("Sesi telah kedaluwarsa");
        }
        // Identity Binding: userId harus sesuai dengan yang ada di dalam token
        if (!userId.equals(extractUserIdFromToken(token))) {
            throw new IllegalArgumentException(
                "Mismatched identity: Token tidak sesuai dengan user id"
            );
        }
        this.userId = userId;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    private static String extractUserIdFromToken(String token) {
        // Simulasi ekstrak userId dari token
        return token.split("\\.")[0];
    }

    public String getUserId() { return userId; }
    public boolean isExpired() { return Instant.now().isAfter(expiresAt); }
}