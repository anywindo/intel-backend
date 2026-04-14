package org.example.securecoding.intelbackend.deep.controller;

import org.example.securecoding.intelbackend.deep.domain.EmployeeSearchQuery;
import org.example.securecoding.intelbackend.deep.domain.VerifiedSession;
import org.example.securecoding.intelbackend.deep.service.SecureBusinessCardAPI;
import org.example.securecoding.intelbackend.model.Employee;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * DEEP MODEL — Controller: Business Card Portal
 * 
 * This controller demonstrates the 'Secure by Design' approach by translating
 * raw primitive types (Strings) into Domain Primitives as soon as they enter
 * the system.
 */
@RestController
@RequestMapping("/api/deep/business-card")
@CrossOrigin(origins = "*")
public class BusinessCardDeepController {

    private final SecureBusinessCardAPI businessCardAPI;

    public BusinessCardDeepController(SecureBusinessCardAPI businessCardAPI) {
        this.businessCardAPI = businessCardAPI;
    }

    /**
     * VULNERABILITY MITIGATION:
     * Unlike the Shallow model which blindly trusts a boolean 'isAuthenticated' flag,
     * this endpoint forces the creation of a 'VerifiedSession'.
     */
    @PostMapping("/search")
    public ResponseEntity<?> search(@RequestBody Map<String, String> request) {
        try {
            String userId = request.get("userId");
            String token = request.get("token");
            String queryText = request.get("query");

            // Build Domain Primitives — validation happens in constructors
            VerifiedSession session = new VerifiedSession(userId, token);
            EmployeeSearchQuery query = new EmployeeSearchQuery(queryText);

            // Pass verified objects to the service
            List<Employee> results = businessCardAPI.searchEmployees(session, query);
            return ResponseEntity.ok(results);
            
        } catch (IllegalArgumentException e) {
            // SECURE ERROR HANDLING: Avoid leaking stack traces, 
            // only return the specific invariant violation.
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "An unexpected error occurred"));
        }
    }
}
