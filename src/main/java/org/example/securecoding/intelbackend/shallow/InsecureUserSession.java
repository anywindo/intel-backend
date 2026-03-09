package org.example.securecoding.intelbackend.shallow;

/**
 * SHALLOW MODEL — VULNERABILITY 1: Anemic Domain Model & Boolean Flag Hell
 *
 * Authentication state is represented by a plain boolean that can be freely
 * manipulated without any backend verification. This mirrors the Intel breach
 * where the attacker modified the MSAL JavaScript 'getAllAccounts' function
 * to trick the frontend into believing a valid user was logged in.
 *
 * THE FLAW: There is no server-side session validation. The 'isAuthenticated'
 * boolean can be set to true by ANYONE — the system blindly trusts it.
 */
public class InsecureUserSession {

    private String username;
    private String msalToken;
    private boolean isAuthenticated;

    public InsecureUserSession(String username, String msalToken, boolean isAuthenticated) {
        this.username = username;
        this.msalToken = msalToken;
        // VULNERABILITY: No verification whatsoever — the caller decides auth state
        this.isAuthenticated = isAuthenticated;
    }

    // VULNERABILITY: Anyone can flip this flag to bypass authentication
    public void setAuthenticated(boolean authenticated) {
        isAuthenticated = authenticated;
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    public String getUsername() {
        return username;
    }

    public String getMsalToken() {
        return msalToken;
    }
}
