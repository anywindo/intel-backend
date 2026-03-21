package org.example.securecoding.intelbackend.shallow;

/**
 * SHALLOW MODEL — VULNERABILITY 1 & 2: Broken JWT Validation & Missing Session Binding
 *
 * This class simulates the session management used by Intel's SEIMS
 * (Supplier EHS IP Management System) Angular application.
 *
 * VULNERABILITY 1 (Broken JWT Validation):
 * The 'isValid()' method accepts ANY non-empty string as a valid Bearer token.
 * In the real breach, the backend literally accepted the error string
 * "Not Autorized" (with typo) as a valid token and granted access to the system.
 * There is NO cryptographic JWT signature verification whatsoever.
 *
 * VULNERABILITY 2 (Missing Session Binding):
 * There is no binding between the token, userId, or IP address.
 * The token can be used by ANYONE who possesses it — there is no way to
 * verify that a token belongs to a specific user or originates from a
 * specific IP address. The session also has no expiry mechanism.
 *
 * THE FLAW: The backend trusts any non-empty string as authentication proof.
 */
public class InsecureSeimsSession {

    private String token;
    private String userId;

    public InsecureSeimsSession() {
        this.token = null;
        this.userId = null;
    }

    public InsecureSeimsSession(String token) {
        // VULNERABILITY 1: No JWT signature verification
        // No expiry check
        // No issuer/audience validation
        this.token = token;
    }

    /**
     * VULNERABILITY 1: Accepts ANY non-empty string as a "valid" token.
     * In the real breach, the string "Not Autorized" (with typo)
     * was accepted as a valid Bearer token by the backend.
     */
    public boolean isValid() {
        // THE FLAW: Only checks if token is non-null and non-empty
        // Even "Not Autorized", "hello", or "abc" would pass this check
        return token != null && !token.isEmpty();
    }

    /**
     * VULNERABILITY 2: userId is not bound to the token.
     * Anyone can set any userId regardless of what token they have.
     */
    public void setUserId(String userId) {
        // THE FLAW: No verification that this userId matches the token
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        // THE FLAW: Token can be replaced with any string at any time
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }
}
