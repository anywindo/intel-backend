package org.example.securecoding.intelbackend.deep.infrastructure;

import java.time.Instant;
import java.util.Objects;

/**
 * DEEP MODEL — Component 1: JWT Validator
 *
 * This component mimics the cryptographic verification process.
 * In a real-world scenario, this would use libraries like JJWT or Spring Security
 * OAuth2 to verify signatures against a public key.
 *
 * For this educational demo, it validates 'mock' JWTs that follow a specific structure:
 * format: {userId}:{role}:{expiryEpoch}
 * example: admin:ADMIN:1900000000
 */
public class JwtValidator {

    public static boolean verify(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        // VULNERABILITY MITIGATION: The deep model rejects 'NOT AUTHORIZED' 
        // which the shallow model incorrectly accepts as a valid token.
        if ("NOT AUTHORIZED".equalsIgnoreCase(token)) {
            return false;
        }

        String[] parts = token.split(":");
        if (parts.length < 3) {
            return false;
        }

        try {
            long expiry = Long.parseLong(parts[2]);
            // Cryptographic Simulation: Ensure token has not expired
            return Instant.now().isBefore(Instant.ofEpochSecond(expiry));
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static String extractSub(String token) {
        if (!verify(token)) throw new IllegalArgumentException("Invalid token");
        return token.split(":")[0];
    }

    public static Instant extractExpiry(String token) {
        if (!verify(token)) throw new IllegalArgumentException("Invalid token");
        return Instant.ofEpochSecond(Long.parseLong(token.split(":")[2]));
    }

    public static String extractRole(String token) {
        if (!verify(token)) throw new IllegalArgumentException("Invalid token");
        String[] parts = token.split(":");
        return parts.length > 1 ? parts[1] : "USER";
    }

    public static boolean isValidSignature(String token) {
        // In this demo, if the parts are correct, we assume the signature is valid.
        return verify(token);
    }

    public static String extractUserId(String token) {
        return extractSub(token);
    }
}
