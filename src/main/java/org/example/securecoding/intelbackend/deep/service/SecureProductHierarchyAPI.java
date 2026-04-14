package org.example.securecoding.intelbackend.deep.service;

import org.example.securecoding.intelbackend.deep.domain.VerifiedAuthSession;
import org.example.securecoding.intelbackend.model.Product;
import org.example.securecoding.intelbackend.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * DEEP MODEL — Secure Logic: Product Hierarchy
 *
 * Implements server-side role-based access control (RBAC).
 * Security logic is tied directly to the VerifiedAuthSession domain primitive.
 * This makes it impossible to call the data layer without first passing
 * through the security gate.
 */
@Service
public class SecureProductHierarchyAPI {

    private final ProductRepository productRepository;

    public SecureProductHierarchyAPI(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * VULNERABILITY MITIGATION:
     * Unlike Case B in the shallow model where everyone could see everything,
     * this service strictly enforces the ADMIN role.
     */
    public List<Product> getAllProducts(VerifiedAuthSession session) {
        // ENFORCE RBAC: Only ADMINs can see the full product hierarchy
        if (!session.isAdmin()) {
            // Principle of Least Privilege: Return empty list for unauthorized access
            return Collections.emptyList();
        }
        
        return productRepository.findAll();
    }
}
