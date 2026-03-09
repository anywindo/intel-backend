package org.example.securecoding.intelbackend.shallow;

public class InsecureUserSession {
    private String username;
    private String msalToken;
    private boolean isAuthenticated;

    public InsecureUserSession(String username, String msalToken, boolean isAuthenticated) {
        this.username = username;
        this.msalToken = msalToken;
        this.isAuthenticated = isAuthenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        isAuthenticated = authenticated;
    }

    public String getUsername() {
        return username;
    }

    public String getMsalToken() {
        return msalToken;
    }
}
