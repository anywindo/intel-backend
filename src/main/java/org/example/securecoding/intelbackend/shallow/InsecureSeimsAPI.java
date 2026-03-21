package org.example.securecoding.intelbackend.shallow;

import org.example.securecoding.intelbackend.model.Supplier;
import org.example.securecoding.intelbackend.model.SupplierNda;
import org.example.securecoding.intelbackend.repository.SupplierNdaRepository;
import org.example.securecoding.intelbackend.repository.SupplierRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * SHALLOW MODEL — VULNERABILITY 3, 4 & 5: Sequential ID Enumeration,
 * No Authorization Scoping, and NDA Exposure
 *
 * This service simulates the vulnerable backend of Intel's SEIMS system.
 * In the real breach:
 *
 * VULNERABILITY 3 (Sequential ID Enumeration / IDOR):
 * Supplier IDs are sequential integers (1, 2, 3, ...). Combined with
 * no rate limiting and no real authentication, an attacker can trivially
 * enumerate all suppliers by iterating: /suppliers/1, /suppliers/2, ...
 *
 * VULNERABILITY 4 (No Authorization Scoping):
 * The 'getAllSuppliers()' method dumps the entire supplier table.
 * There is no scoping by user role, department, or any other criterion.
 * Any "authenticated" user (i.e., anyone with a non-empty token) sees everything.
 *
 * VULNERABILITY 5 (NDA Exposure Without Auth Scoping):
 * Confidential and Top Secret NDAs are returned to any user who provides
 * a valid supplier ID. There is no classification-based access control.
 */
@Service
public class InsecureSeimsAPI {

    private final SupplierRepository supplierRepository;
    private final SupplierNdaRepository supplierNdaRepository;

    public InsecureSeimsAPI(SupplierRepository supplierRepository,
                            SupplierNdaRepository supplierNdaRepository) {
        this.supplierRepository = supplierRepository;
        this.supplierNdaRepository = supplierNdaRepository;
    }

    /**
     * VULNERABILITY 1 (via InsecureSeimsSession):
     * Creates a session from any Bearer token string.
     * Even "Not Autorized" will be accepted as valid.
     */
    public InsecureSeimsSession authenticate(String bearerToken) {
        InsecureSeimsSession session = new InsecureSeimsSession(bearerToken);

        // THE FLAW: If the token is non-empty, we consider it "valid"
        if (session.isValid()) {
            session.setUserId("anonymous");  // No real identity binding
            return session;
        }

        throw new SecurityException("Error: Not Autorized");  // Typo matches real breach
    }

    /**
     * VULNERABILITY 3: Get supplier by sequential ID (IDOR).
     * Attacker can enumerate: /suppliers/1, /suppliers/2, ..., /suppliers/N
     * No rate limiting, no authorization check beyond boolean session validity.
     */
    public Supplier getSupplierById(Long id, InsecureSeimsSession session) {
        // THE FLAW: Only checks boolean validity — no real auth
        if (!session.isValid()) {
            throw new SecurityException("Error: Not Autorized");
        }

        // Sequential ID makes this trivially enumerable
        return supplierRepository.findById(id).orElse(null);
    }

    /**
     * VULNERABILITY 4: Dumps the entire supplier table.
     * No authorization scoping — any "authenticated" user sees all suppliers.
     */
    public List<Supplier> getAllSuppliers(InsecureSeimsSession session) {
        if (!session.isValid()) {
            throw new SecurityException("Error: Not Autorized");
        }

        // DISASTER: Returns ALL suppliers without any scoping
        return supplierRepository.findAll();
    }

    /**
     * VULNERABILITY 5: Exposes confidential NDAs without classification-based access control.
     * Both "Confidential" and "Top Secret" NDAs are returned to any user
     * who can provide a valid supplier ID — which is trivial due to sequential IDs.
     */
    public List<SupplierNda> getNdaBySupplier(Long supplierId, InsecureSeimsSession session) {
        if (!session.isValid()) {
            throw new SecurityException("Error: Not Autorized");
        }

        // THE FLAW: No classification-based filtering
        // Top Secret NDAs returned alongside Confidential ones
        return supplierNdaRepository.findBySupplierId(supplierId);
    }
}
