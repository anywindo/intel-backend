package org.example.securecoding.intelbackend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * JPA Entity representing a Non-Disclosure Agreement (NDA) between Intel and a supplier.
 *
 * SHALLOW MODEL — Case C: SEIMS
 *
 * In the real breach, confidential NDAs containing sensitive IP information
 * were exposed without proper authorization checks. An attacker who
 * enumerated supplier IDs could access all associated NDAs, including
 * those classified as "Top Secret".
 *
 * This entity stores NDA metadata including classification level and
 * document URLs — all accessible without authentication in the Shallow Model.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierNda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    private String ndaTitle;
    private LocalDate signedDate;
    private LocalDate expiryDate;

    // Classification: 'Confidential' or 'Top Secret'
    private String classification;

    // Direct URL to the NDA document — exposed without auth in Shallow Model
    private String documentUrl;
}
