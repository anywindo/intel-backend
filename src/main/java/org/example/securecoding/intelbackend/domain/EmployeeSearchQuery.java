package org.example.securecoding.intelbackend.domain;

/**
 * DEEP MODEL PLACEHOLDER: EmployeeSearchQuery
 * 
 * Secure by Design Principle:
 * This domain primitive prevents VULNERABILITY 3 (Data Over-fetching & Primitive Obsession).
 * Instead of passing a raw String which can be empty or null, this object ensures that
 * the search invariants (e.g., non-empty, minimum length) are always met before the repository is even called.
 */
public final class EmployeeSearchQuery {
    private final String value;

    public EmployeeSearchQuery(String raw) {
        // INVARIANT: Query tidak boleh null, kosong, atau terlalu pendek
        // Dengan meng-enforce ini di konstruktor, mustahil terjadi state 'empty string' 
        // yang membocorkan data (dump entire DB).
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException("Search query tidak boleh kosong");
        }
        if (raw.trim().length() < 2) {
            throw new IllegalArgumentException("Search query minimal 2 karakter");
        }
        this.value = raw.trim();
    }

    public String getValue() {
        return value;
    }
}
