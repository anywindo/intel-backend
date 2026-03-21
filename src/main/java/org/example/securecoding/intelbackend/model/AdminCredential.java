package org.example.securecoding.intelbackend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    // VULNERABILITY: Password stored in plaintext — not hashed
    private String password;

    // VULNERABILITY: Role as primitive String — attacker can spoof "admin"
    private String role;

    // VULNERABILITY: GitHub personal access token exposed in source code
    private String githubToken;

    // VULNERABILITY: Plaintext Basic Auth header
    private String basicAuth;
}
