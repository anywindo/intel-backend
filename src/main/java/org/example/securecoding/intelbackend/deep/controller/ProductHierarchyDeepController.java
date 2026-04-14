package org.example.securecoding.intelbackend.deep.controller;

import org.example.securecoding.intelbackend.deep.domain.VerifiedAuthSession;
import org.example.securecoding.intelbackend.deep.service.SecureProductHierarchyAPI;
import org.example.securecoding.intelbackend.model.Product;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * DEEP MODEL — Controller: Product Hierarchy
 * 
 * Demonstrates secure state management. The controller does not track
 * 'isAuthenticated' boolean; instead, it attempts to construct a 
 * VerifiedAuthSession from the provided token.
 */
@RestController
@RequestMapping("/api/deep/hierarchy")
@CrossOrigin(origins = "*")
public class ProductHierarchyDeepController {

    private final SecureProductHierarchyAPI hierarchyAPI;

    public ProductHierarchyDeepController(SecureProductHierarchyAPI hierarchyAPI) {
        this.hierarchyAPI = hierarchyAPI;
    }

    /**
     * VULNERABILITY MITIGATION:
     * Logic for authentication and role verification is moved entirely to the server.
     */
    @PostMapping("/products")
    public ResponseEntity<?> getProducts(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");

            // Build Domain Primitive — constructor handles validation and parsing
            VerifiedAuthSession session = VerifiedAuthSession.fromValidatedToken(token);

            List<Product> products = hierarchyAPI.getAllProducts(session);
            
            // Check if access was denied due to roles
            if (products.isEmpty() && !session.isAdmin()) {
                return ResponseEntity.status(403).body(Map.of("error", "Access Denied: Admin privileges required to view full hierarchy"));
            }

            return ResponseEntity.ok(products);
            
        } catch (IllegalArgumentException e) {
            // SECURE ERROR HANDLING: Avoid leaking details, only return error message
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "An internal error occurred"));
        }
    }
}
