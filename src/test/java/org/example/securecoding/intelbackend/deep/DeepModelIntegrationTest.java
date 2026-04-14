package org.example.securecoding.intelbackend.deep;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * DEEP MODEL — Integration Test: Security Verification
 *
 * This test suite verifies that the 'Deep Model' correctly enforces security.
 * Uses TestRestTemplate for higher-level integration testing.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DeepModelIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testUnauthorizedAccessToDeepModel_ShouldBeForbidden() {
        Map<String, String> request = new HashMap<>();
        request.put("query", "John");

        ResponseEntity<String> response = restTemplate.postForEntity("/api/deep/business-card/search", request, String.class);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testAuthorizedAccessWithValidToken_ShouldBeOk() {
        String validToken = "admin:ADMIN:1900000000";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(validToken);
        
        Map<String, String> request = new HashMap<>();
        request.put("userId", "admin");
        request.put("token", validToken);
        request.put("query", "John");

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);
        ResponseEntity<String> response = restTemplate.postForEntity("/api/deep/business-card/search", entity, String.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testCaseB_RbacAccessDeniedForUser_ShouldBeForbidden() {
        String userToken = "user1:USER:1900000000";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);
        
        Map<String, String> request = new HashMap<>();
        request.put("token", userToken);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);
        ResponseEntity<String> response = restTemplate.postForEntity("/api/deep/hierarchy/products", entity, String.class);
        
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testCaseA_InvalidQuery_ShouldBeBadRequest() {
        String validToken = "admin:ADMIN:1900000000";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(validToken);
        
        Map<String, String> request = new HashMap<>();
        request.put("userId", "admin");
        request.put("token", validToken);
        request.put("query", "J"); // Too short

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);
        ResponseEntity<String> response = restTemplate.postForEntity("/api/deep/business-card/search", entity, String.class);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
