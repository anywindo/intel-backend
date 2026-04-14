package org.example.securecoding.intelbackend.deep.domain;

/**
 * DEEP MODEL — Domain Primitive: Role
 *
 * Enums are used to prevent string-based role manipulation.
 * The system only recognizes server-defined roles, preventing an attacker
 * from injecting a custom high-privilege role string.
 */
public enum Role {
    USER,
    ADMIN;

    public static Role fromString(String role) {
        if (role == null) return USER;
        try {
            // Principle of Least Privilege: If the role is unknown, default to USER
            return Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            return USER; 
        }
    }
}
