package org.example.securecoding.intelbackend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.securecoding.intelbackend.domain.PasswordHash;

/**
 * JPA Entity representing hardcoded admin credentials.
 *
 * SHALLOW MODEL — Case B: Product Hierarchy System
 *
 * In the real Intel breach, admin credentials (plaintext passwords, Basic Auth
 * headers, and even a GitHub personal access token) were hardcoded directly
 * in the client-side JavaScript source code. This entity simulates that
 * exposure by storing credentials in plaintext in the database.
 *
 * VULNERABILITIES REPRESENTED:
 * - Password stored in plaintext (not hashed)
 * - Role stored as primitive String (not Enum)
 * - GitHub PAT exposed (could create rogue products on Intel ARK)
 * - Basic Auth header stored in plaintext
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminCredential {

    @Id
    private String id;

    private String username;

    // DEEP MODEL: PasswordHash encapsulates the BCrypt hash logic
    private PasswordHash password;

    private String role;

    // Simulation fields for Case B — always masked in Deep Model API responses
    private String githubToken;
    private String basicAuth;

    public AdminCredential(String id, String username, PasswordHash password, String role, String githubToken, String basicAuth) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Credential ID cannot be empty");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty (Deep Model Violation)");
        }
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.githubToken = githubToken;
        this.basicAuth = basicAuth;
    }

    /**
     * === KODE RENTAN (SHALLOW MODEL) — Dipertahankan untuk Komparatif ===
     *
     * @Entity
     * @Data
     * @NoArgsConstructor
     * @AllArgsConstructor
     * public class AdminCredential {
     *     private Long id;
     *     private String username;
     *     private String password;    // Primitive Obsession: Password tersimpan dalam plaintext
     *     private String role;
     *     private String githubToken; // VULNERABILITY: Rahasia pihak ketiga terekspos
     *     private String basicAuth;   // VULNERABILITY: Header otentikasi terekspos
     * }
     */
}
