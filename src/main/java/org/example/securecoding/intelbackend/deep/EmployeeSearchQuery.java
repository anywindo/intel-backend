package org.example.securecoding.intelbackend.deep;

/**
 * DEEP MODEL — FIX untuk Data Over-fetching (Business Card System)
 *
 * Masalah sebelumnya: backend mengandalkan frontend untuk mengirim filter.
 * Jika filter dihapus, backend dump seluruh 270.000 data karyawan.
 *
 * Fix: query wajib melewati domain primitive ini.
 * Konstruktor menolak query kosong atau terlalu pendek.
 */
public class EmployeeSearchQuery {

    private static final int MINIMUM_QUERY_LENGTH = 3;
    private final String value;

    // Constructor langsung validasi — tidak bisa bypass dari luar
    public EmployeeSearchQuery(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Query pencarian tidak boleh kosong");
        }
        if (value.trim().length() < MINIMUM_QUERY_LENGTH) {
            throw new IllegalArgumentException(
                "Query terlalu pendek — minimal " + MINIMUM_QUERY_LENGTH + " karakter"
            );
        }
        this.value = value.trim();
    }

    public String getValue() {
        return value;
    }
}