package org.example.securecoding.intelbackend.shallow;

import org.example.securecoding.intelbackend.model.AdminCredential;
import org.example.securecoding.intelbackend.model.Product;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST Controller exposing the VULNERABLE Product Hierarchy Shallow Model endpoints.
 *
 * These endpoints intentionally simulate the Intel Product Hierarchy System flaws:
 * - /api/shallow/product/login → Plaintext password authentication
 * - /api/shallow/product/hierarchy → Boolean-only auth check, returns all products
 * - /api/shallow/product/credentials → Exposes hardcoded admin credentials
 */
@RestController
@RequestMapping("/api/shallow/product")
public class ProductHierarchyShallowController {

    private final InsecureProductHierarchyAPI insecureProductHierarchyAPI;

    // Stores the last authenticated session (simulates client-side session state)
    private InsecureProductSession currentSession;

    public ProductHierarchyShallowController(InsecureProductHierarchyAPI insecureProductHierarchyAPI) {
        this.insecureProductHierarchyAPI = insecureProductHierarchyAPI;
        this.currentSession = new InsecureProductSession();
    }

    /**
     * VULNERABILITY 3: Login with plaintext credentials.
     * Password is compared via String.equals() — no hashing, no salting.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestParam String username,
            @RequestParam String password) {

        InsecureProductSession session = insecureProductHierarchyAPI.authenticate(username, password);
        this.currentSession = session;

        if (session.isAuthenticated()) {
            return ResponseEntity.ok(Map.of(
                    "authenticated", true,
                    "username", session.getUsername(),
                    "role", session.getRoles()[0]));
        } else {
            return ResponseEntity.status(401).body(Map.of(
                    "authenticated", false,
                    "error", "Error: Invalid Credentials"));
        }
    }

    /**
     * VULNERABILITY 4: Returns product hierarchy if session boolean is true.
     * No server-side session validation — just checks the boolean flag.
     */
    @GetMapping("/hierarchy")
    public ResponseEntity<?> getProductHierarchy() {
        try {
            List<Product> products = insecureProductHierarchyAPI.getProducts(currentSession);
            return ResponseEntity.ok(Map.of(
                    "count", products.size(),
                    "data", products));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * VULNERABILITY 5: Exposes ALL admin credentials.
     * This simulates inspecting the client-side source code where developers
     * hardcoded plaintext passwords, Basic Auth headers, and GitHub tokens.
     *
     * In the real breach, the attacker simply opened the browser DevTools
     * and read these credentials directly from the JavaScript source code.
     */
    @GetMapping("/credentials")
    public ResponseEntity<?> getCredentials() {
        List<AdminCredential> credentials = insecureProductHierarchyAPI.getCredentials();
        return ResponseEntity.ok(Map.of(
                "count", credentials.size(),
                "data", credentials));
    }
}
