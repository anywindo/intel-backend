package org.example.securecoding.intelbackend.util;

import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * MOCK JWT SIMULATOR
 * Disusun untuk tugas keamanan, mensimulasikan parsing, verifikasi
 * kriptografis, dan claim ekstraksi persis menyerupai pustaka io.jsonwebtoken.
 */
public class JwtValidator {

    private static final String SECRET_SIGNATURE = "S3cUr3_M0ck_S1gn4tUr3";

    public static class Claims {
        private final Map<String, Object> map = new HashMap<>();

        public String getSubject() {
            return (String) map.get("sub");
        }

        public String getRole() {
            return (String) map.get("role");
        }

        public String get(String key, Class<String> type) {
            return type.cast(map.get(key));
        }

        public Instant getExpiration() {
            return (Instant) map.get("exp");
        }

        public void put(String key, Object value) {
            map.put(key, value);
        }
    }

    public static String createToken(String subject, String role) {
        String payload = subject + "|" + role + "|" + Instant.now().plusSeconds(3600).toEpochMilli();
        String encoded = Base64.getEncoder().encodeToString(payload.getBytes());
        return "eyJhbGci." + encoded + "." + SECRET_SIGNATURE;
    }

    public static Claims verify(String jwt) {
        if (jwt == null || !jwt.startsWith("eyJhbGci.")) {
            throw new IllegalArgumentException("Invalid JWT format (Mock)");
        }
        String[] parts = jwt.split("\\.");
        if (parts.length != 3 || !SECRET_SIGNATURE.equals(parts[2])) {
            throw new IllegalArgumentException("Invalid JWT signature (Mock)");
        }

        try {
            String decoded = new String(Base64.getDecoder().decode(parts[1]));
            String[] payloadParams = decoded.split("\\|");
            
            Claims claims = new Claims();
            claims.put("sub", payloadParams[0]);
            claims.put("role", payloadParams[1]);
            
            Instant exp = Instant.ofEpochMilli(Long.parseLong(payloadParams[2]));
            if (exp.isBefore(Instant.now())) {
                throw new IllegalArgumentException("JWT is expired");
            }
            claims.put("exp", exp);
            
            return claims;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to decode JWT payload");
        }
    }
}
