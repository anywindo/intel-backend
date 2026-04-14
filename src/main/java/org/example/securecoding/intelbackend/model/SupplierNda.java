package org.example.securecoding.intelbackend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
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
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SupplierNda {

    @Id
    private String id;

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

    public SupplierNda(String id, Supplier supplier, String ndaTitle, LocalDate signedDate, LocalDate expiryDate, String classification, String documentUrl) {
        if (ndaTitle == null || ndaTitle.trim().isEmpty()) {
            throw new IllegalArgumentException("NDA title cannot be empty (Deep Model Violation)");
        }
        this.id = id;
        this.supplier = supplier;
        this.ndaTitle = ndaTitle;
        this.signedDate = signedDate;
        this.expiryDate = expiryDate;
        this.classification = classification;
        this.documentUrl = documentUrl;
    }

    /**
     * === KODE RENTAN (SHALLOW MODEL) — Dipertahankan untuk Komparatif ===
     *
     * @Entity
     * @Data
     * @NoArgsConstructor
     * @AllArgsConstructor
     * public class SupplierNda {
     *     @Id
     *     @GeneratedValue(strategy = GenerationType.IDENTITY)
     *     private Long id;
     *     @ManyToOne
     *     @JoinColumn(name = "supplier_id")
     *     private Supplier supplier;
     *     private String ndaTitle;
     *     private LocalDate signedDate;
     *     private LocalDate expiryDate;
     *     private String classification;
     *     private String documentUrl;
     * }
     */
}
