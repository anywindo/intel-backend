package org.example.securecoding.intelbackend.domain;

/**
 * DEEP MODEL PLACEHOLDER: Role
 * 
 * Secure by Design Principle:
 * This enum prevents Role Spoofing (Primitive Obsession).
 * Instead of an open `String[] roles` that a client can manipulate,
 * the Role is an enumeration strictly defined by the backend server.
 */
public enum Role {
    USER, ADMIN;

    public static Role fromServerToken(String jwtRoleClaim) {
        if ("ADMIN".equalsIgnoreCase(jwtRoleClaim)) return ADMIN;
        return USER; // Default ke privilege terendah (Principle of Least Privilege)
    }
}
