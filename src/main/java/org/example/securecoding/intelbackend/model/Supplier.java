package org.example.securecoding.intelbackend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.securecoding.intelbackend.domain.EmailAddress;
import org.example.securecoding.intelbackend.domain.PhoneNumber;

/**
 * JPA Entity representing a supplier in Intel's SEIMS system.
 *
 * Used by the Shallow Model (Case C) to simulate the Supplier EHS IP
 * Management System that was compromised in the "Intel Outside" breach.
 *
 * VULNERABILITY NOTE: The 'id' field uses sequential BIGINT auto-increment.
 * This makes supplier IDs predictable and trivially enumerable by an attacker
 * (e.g., /api/suppliers/1, /api/suppliers/2, ..., /api/suppliers/N).
 * In the Deep Model, this will be replaced with UUID-based public identifiers.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Supplier {

    @Id
    private String id; // DEEP MODEL: Non-sequential UUID to prevent ID enumeration (Case C)

    private String companyName;
    private String contactPerson;

    // DEEP MODEL: Domain Primitives
    private EmailAddress email;
    private PhoneNumber phone;

    private String country;
    private String ehsStatus;

    public Supplier(String id, String companyName, String contactPerson, EmailAddress email, PhoneNumber phone, String country, String ehsStatus) {
        if (id == null || id.trim().isEmpty()) {
            this.id = java.util.UUID.randomUUID().toString();
        } else {
            this.id = id;
        }
        if (companyName == null || companyName.trim().isEmpty()) {
            throw new IllegalArgumentException("Company name cannot be empty (Deep Model Violation)");
        }
        this.companyName = companyName;
        this.contactPerson = contactPerson;
        this.email = email;
        this.phone = phone;
        this.country = country;
        this.ehsStatus = ehsStatus;
    }

    /**
     * === KODE RENTAN (SHALLOW MODEL) — Dipertahankan untuk Komparatif ===
     *
     * @Entity
     * @Data
     * @NoArgsConstructor
     * @AllArgsConstructor
     * public class Supplier {
     *     @Id
     *     @GeneratedValue(strategy = GenerationType.IDENTITY)
     *     private Long id;              // VULNERABILITY: Sequential — allows trivial enumeration
     *     private String companyName;
     *     private String contactPerson;
     *     private String email;
     *     private String phone;
     *     private String country;
     *     private String ehsStatus;
     * }
     */
}
