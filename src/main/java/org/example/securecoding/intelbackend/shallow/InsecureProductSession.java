package org.example.securecoding.intelbackend.shallow;

/**
 * SHALLOW MODEL — VULNERABILITY 1 & 2: Boolean Auth Bypass & Role Spoofing
 *
 * This class simulates the authentication model used by Intel's
 * "Hierarchy Management" and "Product Onboarding" ReactJS sites.
 *
 * VULNERABILITY 1 (Boolean Auth Bypass):
 * The 'isAuthenticated' flag is a plain boolean with an open setter.
 * In the real breach, the attacker overrode the ReactJS 'isAuthenticated'
 * variable to 'true' at startup, completely bypassing the Azure SSO redirect.
 *
 * VULNERABILITY 2 (Role Spoofing via Primitive Obsession):
 * User roles are stored as a raw 'String[]' with an open setter.
 * In the real breach, the attacker commented out the Microsoft Graph API call
 * (making it a "no-op") and hardcoded the local role array to include
 * "SPARK Product Management System Admin" or "admin", granting full
 * administrative access.
 *
 * THE FLAW: There is no server-side validation of authentication state or roles.
 * The backend blindly trusts whatever the client sends.
 */
public class InsecureProductSession {

    private String username;
    private boolean isAuthenticated;
    private String[] roles;

    public InsecureProductSession() {
        this.isAuthenticated = false;
        this.roles = new String[]{};
    }

    public InsecureProductSession(String username, boolean isAuthenticated, String[] roles) {
        this.username = username;
        // VULNERABILITY 1: No verification — caller decides auth state
        this.isAuthenticated = isAuthenticated;
        // VULNERABILITY 2: Roles set directly from client input
        this.roles = roles;
    }

    /**
     * VULNERABILITY 1: Anyone can flip this flag to bypass authentication.
     * No challenge, no token verification, no server-side session check.
     */
    public void setAuthenticated(boolean authenticated) {
        this.isAuthenticated = authenticated;
    }

    /**
     * VULNERABILITY 2: Roles can be set directly from the client.
     * Attacker hardcodes: setRoles(new String[]{"SPARK Product Management System Admin"})
     */
    public void setRoles(String[] roles) {
        this.roles = roles;
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    public String[] getRoles() {
        return roles;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Checks if the session has a specific role.
     * THE FLAW: This is just a String comparison on a client-controlled array.
     */
    public boolean hasRole(String role) {
        if (roles == null) return false;
        for (String r : roles) {
            if (r.equals(role)) return true;
        }
        return false;
    }
}
