package org.example.securecoding.intelbackend.service;

import org.example.securecoding.intelbackend.domain.Role;
import org.example.securecoding.intelbackend.domain.VerifiedAuthSession;
import org.example.securecoding.intelbackend.model.AdminCredential;
import org.example.securecoding.intelbackend.model.Product;
import org.example.securecoding.intelbackend.repository.AdminCredentialRepository;
import org.example.securecoding.intelbackend.repository.ProductRepository;
import org.example.securecoding.intelbackend.util.JwtValidator;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductHierarchyService {

    private final ProductRepository productRepository;
    private final AdminCredentialRepository adminCredentialRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public ProductHierarchyService(ProductRepository productRepository,
            AdminCredentialRepository adminCredentialRepository) {
        this.productRepository = productRepository;
        this.adminCredentialRepository = adminCredentialRepository;
    }

    // === DEEP MODEL - AKTIF ===
    // Validasi password tetap dilakukan ke DB, namun hasilnya tidak lagi berupa
    // boolean primitif.
    // Sebaliknya, method ini mengembalikan JWT berisi role dari DB untuk dipakai di
    // semua request berikutnya.

    public String authenticateAndIssueToken(String username, String password) {
        Optional<AdminCredential> credential = adminCredentialRepository.findByUsername(username);
        if (credential.isPresent() && passwordEncoder.matches(password, credential.get().getPassword().getValue())) {
            String role = credential.get().getRole() != null ? credential.get().getRole() : "USER";
            return JwtValidator.createToken(username, role);
        }
        throw new SecurityException("Invalid credentials");
        // adminCredentialRepository.findByUsername(username);
        // if (credential.isPresent() &&
        // credential.get().getPassword().equals(password)) {
        // return true;
        // }
        // return false;
    }

    // === DEEP MODEL - AKTIF ===
    // Otorisasi diterapkan melalui VerifiedAuthSession.
    // Session hanya bisa dibuat dari token JWT valid, dan role diambil dari klaim
    // terverifikasi —
    // bukan dari memori controller yang bisa di-inject.
    public List<Product> getProductsSecure(String jwt) {
        VerifiedAuthSession session = VerifiedAuthSession.fromValidatedToken(jwt);
        if (session.getRole() != Role.ADMIN) {
            throw new SecurityException("Akses ditolak: Hanya ADMIN yang diizinkan");
        }
        return productRepository.findAll();

        // === KODE RENTAN (SHALLOW MODEL) ===
        // Penjelasan: Pengecekan otorisasi hanya memandang nilai raw boolean dari luar.
        // Celah: Mudah di-bypass lewat simulasi client-side atau injeksi state
        // controller.
        // if (!isAuthenticated) {
        // throw new SecurityException("Error: Not Authenticated");
        // }
        // return productRepository.findAll();
    }

    // === KODE RENTAN (SHALLOW MODEL) - Dipertahankan sebagai referensi ===
    // Penjelasan: Memaparkan rahasia password dan credential di API secara
    // telanjang utuh.
    // Endpoint semacam ini seharusnya dihapus atau dijaga akses kontrol berlapis
    // tingkat sangat tinggi.
    // === DEEP MODEL - DATA MASKING ===
    // Penjelasan: Memaparkan rahasia password dan credential di API adalah resiko fatal.
    // Data sensitif (PasswordHash, Tokens) kini wajib di-mask kecuali untuk audit internal super-khusus.
    public List<AdminCredential> getCredentials() {
        List<AdminCredential> all = adminCredentialRepository.findAll();
        return all.stream()
            .map(cred -> new AdminCredential(
                cred.getId(),
                cred.getUsername(),
                null, // Password hash disembunyikan total
                cred.getRole(),
                "[MASKED]",
                "[MASKED]"
            ))
            .toList();
    }
}
