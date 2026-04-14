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

/**
 * JPA Entity representing a product in Intel's product hierarchy (ARK database).
 *
 * Used by both the Shallow Model (Case B) and the Deep Model to simulate
 * the Product Hierarchy Management system that was compromised in the
 * "Intel Outside" breach.
 *
 * The product table supports self-referencing hierarchy via the 'parent' field,
 * allowing categories (e.g., Processors) to have child products (e.g., Core i9-14900K).
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Product {

    @Id
    private String id;

    private String productName;
    private String productCode;
    private String category;
    private String status;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Product parent;

    private String createdBy;

    // DEEP MODEL: Invariant Enforcement
    public Product(String productName, String productCode, String category, String status, Product parent, String createdBy) {
        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty (Deep Model Violation)");
        }
        if (productCode == null || productCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Product code cannot be empty (Deep Model Violation)");
        }
        this.productName = productName;
        this.productCode = productCode;
        this.category = category;
        this.status = status;
        this.parent = parent;
        this.createdBy = createdBy;
    }

    /**
     * === KODE RENTAN (SHALLOW MODEL) — Dipertahankan untuk Komparatif ===
     *
     * @Entity
     * @Data
     * @NoArgsConstructor
     * @AllArgsConstructor
     * public class Product {
     *     private Long id;
     *     private String productName;
     *     private String productCode;
     *     private String category;
     *     private String status;
     *     @ManyToOne
     *     @JoinColumn(name = "parent_id")
     *     private Product parent;
     *     private String createdBy; // Primitive Obsession: String tidak mencegah identitas palsu
     * }
     */
}
