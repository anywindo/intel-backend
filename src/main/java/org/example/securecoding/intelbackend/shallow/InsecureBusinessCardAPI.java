package org.example.securecoding.intelbackend.shallow;

import org.example.securecoding.intelbackend.model.Employee;
import org.example.securecoding.intelbackend.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * SHALLOW MODEL — VULNERABILITY 2 & 3: Unauthenticated API + Primitive
 * Obsession
 *
 * This service simulates the two critical backend flaws from the Intel breach:
 *
 * VULNERABILITY 2 (Unauthenticated Token API):
 * The 'generateAnonymousToken()' method hands out a highly privileged token
 * to ANY caller without authentication. In the real breach, the
 * 'getAccessToken'
 * API returned valid Bearer tokens to anonymous users.
 *
 * VULNERABILITY 3 (Data Over-fetching & Primitive Obsession):
 * The 'searchFilter' parameter is a raw primitive String with NO domain
 * invariants.
 * There are no rules enforcing minimum length, non-null, or non-empty
 * constraints.
 * When the attacker removes the URL filter (sending null or empty string), the
 * system executes 'findAll()' and dumps the ENTIRE employee database.
 */
@Service
public class InsecureBusinessCardAPI {

    private static final String PRIVILEGED_TOKEN = "SUPER_PRIVILEGED_TOKEN_123";

    private final EmployeeRepository employeeRepository;

    public InsecureBusinessCardAPI(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    /**
     * VULNERABILITY 2: Generates a privileged token without ANY authentication.
     * No session check, no credentials required — anyone can call this.
     */
    public String generateAnonymousToken() {
        return PRIVILEGED_TOKEN;
    }

    /**
     * VULNERABILITY 3: Primitive Obsession — the searchFilter is a raw String.
     * No invariants (rules) enforce that it must be non-null, non-empty, or > 3
     * chars.
     *
     * THE INVALID STATE: If searchFilter is null or empty, the system dumps
     * the entire database — simulating the 1 GB JSON leak of 270,000 records.
     */
    public List<Employee> getEmployeeData(String token, String searchFilter) {
        // Basic token check (but anyone can get this token anonymously!)
        if (!PRIVILEGED_TOKEN.equals(token)) {
            throw new SecurityException("Error: Invalid Token");
        }

        // THE FLAW: No invariant on the search filter.
        // An empty/null filter triggers a full database dump.
        if (searchFilter == null || searchFilter.trim().isEmpty()) {
            // DISASTER: Returns ALL employee records — the data breach!
            return employeeRepository.findAll();
        }

        // Normal operation: returns filtered results for a specific employee
        return employeeRepository.findByFullNameContainingIgnoreCase(searchFilter);
    }
}
