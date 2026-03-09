package org.example.securecoding.intelbackend.shallow;

import org.example.securecoding.intelbackend.model.Employee;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST Controller exposing the VULNERABLE Shallow Model endpoints.
 *
 * These endpoints intentionally simulate the Intel Business Card System flaws:
 * - /api/shallow/token → Anonymous token generation (no auth required)
 * - /api/shallow/employees → Data over-fetching when search filter is empty
 */
@RestController
@RequestMapping("/api/shallow")
public class ShallowController {

    private final InsecureBusinessCardAPI insecureBusinessCardAPI;

    public ShallowController(InsecureBusinessCardAPI insecureBusinessCardAPI) {
        this.insecureBusinessCardAPI = insecureBusinessCardAPI;
    }

    /**
     * VULNERABILITY 2: Unauthenticated token endpoint.
     * Any anonymous user can request a highly privileged token.
     * No credentials, no session, no authentication required.
     */
    @GetMapping("/token")
    public ResponseEntity<Map<String, String>> getAccessToken() {
        String token = insecureBusinessCardAPI.generateAnonymousToken();
        return ResponseEntity.ok(Map.of("accessToken", token));
    }

    /**
     * VULNERABILITY 3: Data over-fetching endpoint.
     * If the 'search' parameter is omitted or empty, the entire employee
     * database is dumped — simulating the 1 GB JSON exfiltration.
     *
     * Normal usage: GET /api/shallow/employees?token=...&search=Eaton → 1 record
     * Attack vector: GET /api/shallow/employees?token=...&search= → ALL records
     */
    @GetMapping("/employees")
    public ResponseEntity<?> getEmployeeData(
            @RequestParam String token,
            @RequestParam(defaultValue = "") String search) {

        try {
            List<Employee> employees = insecureBusinessCardAPI.getEmployeeData(token, search);
            return ResponseEntity.ok(Map.of(
                    "count", employees.size(),
                    "data", employees));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }
}
