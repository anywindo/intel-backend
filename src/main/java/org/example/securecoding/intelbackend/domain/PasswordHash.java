package org.example.securecoding.intelbackend.domain;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * DEEP MODEL: PasswordHash (Domain Primitive)
 * 
 * Secure by Design Principle:
 * By encapsulating the hashed password in its own type, we make it impossible 
 * for the system to accidentally treat a plaintext password as a hash (Type Safety).
 * It also centralizes the invariant that a hash must follow the BCrypt format.
 */
public final class PasswordHash {

    private final String hash;

    public PasswordHash(String hash) {
        if (hash == null || !hash.startsWith("$2a$")) {
            throw new IllegalArgumentException("Invalid BCrypt hash format (Deep Model verification failed)");
        }
        this.hash = hash;
    }

    @JsonValue
    public String getValue() {
        return hash;
    }

    @Override
    public String toString() {
        return "[HASHED]";
    }
}
