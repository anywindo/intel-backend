package org.example.securecoding.intelbackend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // VULNERABILITY: Sequential — allows trivial enumeration

    private String companyName;
    private String contactPerson;
    private String email;
    private String phone;
    private String country;
    private String ehsStatus;
}
