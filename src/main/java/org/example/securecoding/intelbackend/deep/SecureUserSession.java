package org.example.securecoding.intelbackend.deep;

/**
 * DEEP MODEL — FIX untuk InsecureUserSession
 *
 * Perbaikan: AuthenticationState tidak bisa dimanipulasi dari luar.
 * Session hanya bisa dibuat kalau token valid, dan status tidak bisa
 * diubah setelah objek dibuat (immutable).
 */
public class SecureUserSession {

    private final String username;
    private final String msalToken;
    private final boolean authenticated;

    // Constructor private — tidak bisa dibuat langsung dari luar
    private SecureUserSession(String username, String msalToken) {
        this.username = username;
        this.msalToken = msalToken;
        this.authenticated = true;
    }

    /**
     * Satu-satunya cara membuat session.
     * Validasi dilakukan DI SINI, bukan diserahkan ke pemanggil.
     */
    public static SecureUserSession create(String username, String msalToken) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username tidak boleh kosong");
        }
        if (msalToken == null || msalToken.isBlank()) {
            throw new IllegalArgumentException("Token tidak boleh kosong");
        }
        if (!isValidToken(msalToken)) {
            throw new SecurityException("Token tidak valid — akses ditolak");
        }
        return new SecureUserSession(username, msalToken);
    }

    private static boolean isValidToken(String token) {
        // Di sistem nyata: verifikasi signature JWT di sini
        return token.length() > 20;
    }

    // TIDAK ADA setAuthenticated() — status tidak bisa diubah dari luar
    public boolean isAuthenticated() {
        return authenticated;
    }

    public String getUsername() {
        return username;
    }

    public boolean hasToken() {
        return msalToken != null && !msalToken.isBlank();
    }
}