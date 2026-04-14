package org.example.securecoding.intelbackend.deep.domain;

/**
 * DEEP MODEL — Domain Primitive: EmployeeSearchQuery
 *
 * Prevents 'Insecure Direct Object Reference' (IDOR) variants and 'Mass Data Leaks' 
 * by enforcing search criteria at the type level.
 *
 * In Case A, this prevents the 'Search All' scenario (searching with empty string)
 * which would otherwise dump the entire employee database.
 */
public final class EmployeeSearchQuery {
    private final String value;

    public EmployeeSearchQuery(String raw) {
        // INVARIANT ENFORCEMENT: Disallow empty/blank searches
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException("Search query cannot be empty");
        }
        
        // SECURITY CONSTRAINT: Prevent enumeration by requiring specific queries
        if (raw.trim().length() < 2) {
            throw new IllegalArgumentException("Search query must be at least 2 characters for security reasons");
        }
        
        this.value = raw.trim();
    }

    public String getValue() {
        return value;
    }

    // NO SETTERS — immutable
}
