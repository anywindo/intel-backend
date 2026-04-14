package org.example.securecoding.intelbackend.deep.controller;

import org.example.securecoding.intelbackend.deep.domain.SecureSession;
import org.example.securecoding.intelbackend.deep.service.SecureSeimsAPI;
import org.example.securecoding.intelbackend.model.Employee;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * DEEP MODEL — Controller: SEIMS
 * 
 * Demonstrates advanced defensive programming. This endpoint requires 
 * multi-factor binding (UserId, Token, IP, Expiry) to be passed.
 * The 'Secure by Design' architecture ensures that if any of these
 * are manipulated in transit, the back-end will reject the request.
 */
@RestController
@RequestMapping("/api/deep/seims")
@CrossOrigin(origins = "*")
public class SeimsDeepController {

    private final SecureSeimsAPI seimsAPI;

    public SeimsDeepController(SecureSeimsAPI seimsAPI) {
        this.seimsAPI = seimsAPI;
    }

    /**
     * VULNERABILITY MITIGATION:
     * SEIMS data access is protected by strict identity binding and expiry checks.
     */
    @PostMapping("/employees")
    public ResponseEntity<?> getEmployees(@RequestBody Map<String, Object> request) {
        try {
            String userId = (String) request.get("userId");
            String token = (String) request.get("token");
            String ipAddress = (String) request.get("ipAddress");
            Number expiryLong = (Number) request.get("expiresAt");
            
            if (expiryLong == null) {
                throw new IllegalArgumentException("expiryAt is required");
            }

            Instant expiresAt = Instant.ofEpochSecond(expiryLong.longValue());

            // Build Domain Primitive — multi-factor verification happens here
            SecureSession session = new SecureSession(userId, token, ipAddress, expiresAt);

            // Access data using the verified session
            List<Employee> employees = seimsAPI.getSensitiveData(session);
            return ResponseEntity.ok(employees);

        } catch (IllegalArgumentException e) {
            // SECURE ERROR HANDLING: Avoid leaking info
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "An internal error occurred"));
        }
    }
}
