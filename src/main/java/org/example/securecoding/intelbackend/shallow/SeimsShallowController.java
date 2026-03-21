package org.example.securecoding.intelbackend.shallow;

import org.example.securecoding.intelbackend.model.Supplier;
import org.example.securecoding.intelbackend.model.SupplierNda;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST Controller exposing the VULNERABLE SEIMS Shallow Model endpoints.
 *
 * These endpoints intentionally simulate the Intel SEIMS system flaws:
 * - /api/shallow/seims/auth → Accepts any string as Bearer token (broken JWT)
 * - /api/shallow/seims/suppliers → Dumps all suppliers (no auth scoping)
 * - /api/shallow/seims/suppliers/{id} → Sequential ID enumeration (IDOR)
 * - /api/shallow/seims/nda/{supplierId} → Exposes confidential NDAs
 */
@RestController
@RequestMapping("/api/shallow/seims")
public class SeimsShallowController {

    private final InsecureSeimsAPI insecureSeimsAPI;

    // Stores the current session (simulates client-side session state)
    private InsecureSeimsSession currentSession;

    public SeimsShallowController(InsecureSeimsAPI insecureSeimsAPI) {
        this.insecureSeimsAPI = insecureSeimsAPI;
        this.currentSession = new InsecureSeimsSession();
    }

    /**
     * VULNERABILITY 1: Accepts ANY non-empty Bearer token string.
     * In the real breach, "Not Autorized" (with typo) was accepted as valid.
     *
     * Usage: curl -X POST http://localhost:8080/api/shallow/seims/auth -H "Authorization: Bearer Not Autorized"
     */
    @PostMapping("/auth")
    public ResponseEntity<?> authenticate(
            @RequestHeader(value = "Authorization", defaultValue = "") String authHeader) {

        // Extract Bearer token from header
        String token = authHeader.startsWith("Bearer ")
                ? authHeader.substring(7)
                : authHeader;

        try {
            InsecureSeimsSession session = insecureSeimsAPI.authenticate(token);
            this.currentSession = session;

            return ResponseEntity.ok(Map.of(
                    "authenticated", true,
                    "token", session.getToken(),
                    "userId", session.getUserId()));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * VULNERABILITY 4: Dumps the entire supplier table.
     * No authorization scoping — everyone sees all suppliers.
     */
    @GetMapping("/suppliers")
    public ResponseEntity<?> getAllSuppliers() {
        try {
            List<Supplier> suppliers = insecureSeimsAPI.getAllSuppliers(currentSession);
            return ResponseEntity.ok(Map.of(
                    "count", suppliers.size(),
                    "data", suppliers));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * VULNERABILITY 3: Sequential ID enumeration (IDOR).
     * Attacker iterates: /suppliers/1, /suppliers/2, ..., /suppliers/N
     */
    @GetMapping("/suppliers/{id}")
    public ResponseEntity<?> getSupplierById(@PathVariable Long id) {
        try {
            Supplier supplier = insecureSeimsAPI.getSupplierById(id, currentSession);
            if (supplier == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Supplier not found"));
            }
            return ResponseEntity.ok(Map.of("data", supplier));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * VULNERABILITY 5: Exposes confidential NDAs including "Top Secret" documents.
     * No classification-based access control — anyone with a supplier ID sees all NDAs.
     */
    @GetMapping("/nda/{supplierId}")
    public ResponseEntity<?> getNdaBySupplier(@PathVariable Long supplierId) {
        try {
            List<SupplierNda> ndas = insecureSeimsAPI.getNdaBySupplier(supplierId, currentSession);
            return ResponseEntity.ok(Map.of(
                    "count", ndas.size(),
                    "data", ndas));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }
}
