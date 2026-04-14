package org.example.securecoding.intelbackend.service;

import org.example.securecoding.intelbackend.domain.Role;
import org.example.securecoding.intelbackend.domain.SecureSession;
import org.example.securecoding.intelbackend.model.Supplier;
import org.example.securecoding.intelbackend.model.SupplierNda;
import org.example.securecoding.intelbackend.repository.SupplierNdaRepository;
import org.example.securecoding.intelbackend.repository.SupplierRepository;
import org.example.securecoding.intelbackend.util.JwtValidator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeimsService {

    private final SupplierRepository supplierRepository;
    private final SupplierNdaRepository supplierNdaRepository;

    public SeimsService(SupplierRepository supplierRepository, SupplierNdaRepository supplierNdaRepository) {
        this.supplierRepository = supplierRepository;
        this.supplierNdaRepository = supplierNdaRepository;
    }

    // === DEEP MODEL - AKTIF ===
    // Token divalidasi secara kriptografis DAN identitasnya diikat ke IP pemanggil melalui SecureSession.
    // Token yang dicuri dari IP lain tidak akan bisa menghasilkan session yang valid.
    public SecureSession authenticateSecure(String jwt, String callerIp) {
        JwtValidator.Claims claims = JwtValidator.verify(jwt);
        return new SecureSession(
            claims.getSubject(),
            Role.fromServerToken(claims.getRole()),
            jwt,
            callerIp,
            claims.getExpiration()
        );
    }

    // === DEEP MODEL - AKTIF ===
    // Seluruh akses data kini memerlukan objek SecureSession yang immutable.
    // Tanpa session valid yang terikat pada identitas dan IP, eksekusi tidak bisa dilanjutkan.
    public Supplier getSupplierById(String id, SecureSession session) {
        if (session == null) throw new SecurityException("Sesi tidak valid");
        return supplierRepository.findById(id).orElse(null);
    }

    public List<Supplier> getAllSuppliers(SecureSession session) {
        if (session == null) throw new SecurityException("Sesi tidak valid");
        return supplierRepository.findAll();
    }

    public List<SupplierNda> getNdaBySupplier(String supplierId, SecureSession session) {
        if (session == null) throw new SecurityException("Sesi tidak valid");
        
        List<SupplierNda> allNdas = supplierNdaRepository.findBySupplierId(supplierId);

        // === DEEP MODEL - ROLE-BASED FILTERING ===
        // 1. Jika bukan ADMIN, filter data 'Top Secret' agar tidak muncul sama sekali.
        // 2. Jika bukan ADMIN, sembunyikan (mask) URL dokumen sensitif.
        if (session.getRole() != Role.ADMIN) {
            return allNdas.stream()
                .filter(nda -> ! "Top Secret".equalsIgnoreCase(nda.getClassification()))
                .map(nda -> new SupplierNda(
                    nda.getId(),
                    nda.getSupplier(),
                    nda.getNdaTitle(),
                    nda.getSignedDate(),
                    nda.getExpiryDate(),
                    nda.getClassification(),
                    "[REDACTED - ADMIN ONLY]"
                ))
                .toList();
        }
        
        return allNdas;
    }
}
