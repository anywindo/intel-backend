package org.example.securecoding.intelbackend.deep;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * DEEP MODEL — Integration Test: Comprehensive Security Verification
 *
 * This test suite verifies that the 'Deep Model' correctly enforces security
 * across all three business systems (Business Card, Product Hierarchy, SEIMS).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DeepModelIntegrationTest {

    @LocalServerPort
    private int port;

    private TestRestTemplate restTemplate;

    @BeforeEach
    public void setup() {
        // Manually instantiate to avoid autowiring issues in this environment
        this.restTemplate = new TestRestTemplate();
    }

    // --- CASE A: Business Card (Input Sanitization & Secure Session) ---

    @Test
    public void testCaseA_UnauthorizedAccess_ShouldBeForbidden() {
        Map<String, String> request = new HashMap<>();
        request.put("query", "John");

        ResponseEntity<String> response = restTemplate.postForEntity(getBaseUrl() + "/api/deep/business-card/search", request, String.class);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testCaseA_ValidAccess_ShouldBeOk() {
        String validToken = "admin:ADMIN:1900000000";
        HttpHeaders headers = createAuthHeaders(validToken);
        
        Map<String, String> request = new HashMap<>();
        request.put("userId", "admin");
        request.put("token", validToken);
        request.put("query", "John");

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(getBaseUrl() + "/api/deep/business-card/search", entity, String.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testCaseA_InvalidQuery_ShouldBeBadRequest() {
        String validToken = "admin:ADMIN:1900000000";
        HttpHeaders headers = createAuthHeaders(validToken);
        
        Map<String, String> request = new HashMap<>();
        request.put("userId", "admin");
        request.put("token", validToken);
        request.put("query", "J"); // Too short (min 2)

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(getBaseUrl() + "/api/deep/business-card/search", entity, String.class);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // --- CASE B: Product Hierarchy (RBAC Enforcement) ---

    @Test
    public void testCaseB_AdminAccess_ShouldBeOk() {
        String adminToken = "admin:ADMIN:1900000000";
        HttpHeaders headers = createAuthHeaders(adminToken);
        
        Map<String, String> request = new HashMap<>();
        request.put("token", adminToken);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(getBaseUrl() + "/api/deep/hierarchy/products", entity, String.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testCaseB_UserAccess_ShouldBeForbidden() {
        String userToken = "employee:USER:1900000000";
        HttpHeaders headers = createAuthHeaders(userToken);
        
        Map<String, String> request = new HashMap<>();
        request.put("token", userToken);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(getBaseUrl() + "/api/deep/hierarchy/products", entity, String.class);
        
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    // --- CASE C: SEIMS (Multi-Factor Session Binding) ---

    @Test
    public void testCaseC_InvalidSessionBinding_ShouldBeForbidden() {
        // Token belongs to 'admin', but we try to supply 'malicious' in the body
        String adminToken = "admin:ADMIN:1900000000";
        HttpHeaders headers = createAuthHeaders(adminToken);
        
        Map<String, String> request = new HashMap<>();
        request.put("userId", "malicious");
        request.put("token", adminToken);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(getBaseUrl() + "/api/deep/seims/employees", entity, String.class);
        
        // The Domain Primitive SecureSession checks if userId in token matches userId in request
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    private HttpHeaders createAuthHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }
}
