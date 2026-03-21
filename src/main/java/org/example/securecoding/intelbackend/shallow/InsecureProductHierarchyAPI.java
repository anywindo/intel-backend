package org.example.securecoding.intelbackend.shallow;

import org.example.securecoding.intelbackend.model.AdminCredential;
import org.example.securecoding.intelbackend.model.Product;
import org.example.securecoding.intelbackend.repository.AdminCredentialRepository;
import org.example.securecoding.intelbackend.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * SHALLOW MODEL — VULNERABILITY 3, 4 & 5: Hardcoded Credentials,
 * Boolean-Only Auth Check, and Credential Exposure
 *
 * This service simulates the vulnerable backend of Intel's Product Hierarchy
 * Management system. In the real breach:
 *
 * VULNERABILITY 3 (Plaintext Password Comparison):
 * Admin passwords were stored as plaintext strings. The 'authenticate()' method
 * compares passwords with a simple String.equals() — no hashing, no salting.
 *
 * VULNERABILITY 4 (Boolean-Only Auth Check):
 * The 'getProducts()' method only checks the boolean 'isAuthenticated' flag
 * on the session object. Since this flag can be set to true by anyone
 * (see InsecureProductSession), access control is effectively nonexistent.
 *
 * VULNERABILITY 5 (Credential Exposure):
 * The 'getCredentials()' method returns all admin credentials including
 * plaintext passwords, GitHub tokens, and Basic Auth headers. In the real
 * breach, these were found hardcoded in the client-side JavaScript source code.
 */
@Service
public class InsecureProductHierarchyAPI {

    private final ProductRepository productRepository;
    private final AdminCredentialRepository adminCredentialRepository;

    public InsecureProductHierarchyAPI(ProductRepository productRepository,
                                       AdminCredentialRepository adminCredentialRepository) {
        this.productRepository = productRepository;
        this.adminCredentialRepository = adminCredentialRepository;
    }

    /**
     * VULNERABILITY 3: Authenticates using plaintext password comparison.
     * No hashing, no salting, no rate limiting — just String.equals().
     *
     * Returns an InsecureProductSession with the role from the database.
     */
    public InsecureProductSession authenticate(String username, String password) {
        Optional<AdminCredential> credential = adminCredentialRepository.findByUsername(username);

        if (credential.isPresent() && credential.get().getPassword().equals(password)) {
            // THE FLAW: Password compared in plaintext — no bcrypt, no SHA
            InsecureProductSession session = new InsecureProductSession();
            session.setUsername(username);
            session.setAuthenticated(true);
            session.setRoles(new String[]{credential.get().getRole()});
            return session;
        }

        // Return unauthenticated session
        InsecureProductSession session = new InsecureProductSession();
        session.setUsername(username);
        session.setAuthenticated(false);
        return session;
    }

    /**
     * VULNERABILITY 4: Only checks the boolean isAuthenticated flag.
     * Since that flag can be set to 'true' by anyone (no server verification),
     * this is effectively an unprotected endpoint.
     *
     * If authenticated, returns ALL products regardless of user role.
     * No authorization scoping — admin and viewer see the same data.
     */
    public List<Product> getProducts(InsecureProductSession session) {
        // THE FLAW: Only checks a boolean flag — no server-side session validation
        if (!session.isAuthenticated()) {
            throw new SecurityException("Error: Not Authenticated");
        }

        // Returns ALL products without any role-based filtering
        return productRepository.findAll();
    }

    /**
     * VULNERABILITY 5: Exposes ALL admin credentials including plaintext
     * passwords, GitHub tokens, and Basic Auth headers.
     *
     * This simulates the real breach where developers hardcoded credentials
     * in the client-side JavaScript source code, making them trivially
     * discoverable by anyone who inspected the browser's source code.
     */
    public List<AdminCredential> getCredentials() {
        // DISASTER: Returns plaintext passwords, GitHub PATs, and Basic Auth headers
        return adminCredentialRepository.findAll();
    }
}
