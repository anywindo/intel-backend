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
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String productName;
    private String productCode;
    private String category;
    private String status;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Product parent;

    private String createdBy;
}
