package org.example.securecoding.intelbackend.controller;

import org.example.securecoding.intelbackend.domain.SecureSession;
import org.example.securecoding.intelbackend.service.SeimsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/seims")
public class SeimsController {

    private final SeimsService seimsService;

    // === KODE RENTAN (SHALLOW MODEL) ===
    // Penjelasan: Mensimulasikan session pengguna di kondisi awal dengan boolean primitif.
    // Celah: State boolean ini tidak terikat identitas dan bisa dimanipulasi antar-request.
    // private boolean currentSessionValid = false;

    public SeimsController(SeimsService seimsService) {
        this.seimsService = seimsService;
    }

    // === DEEP MODEL - AKTIF ===
    // Token divalidasi secara kriptografis DAN identitasnya diikat ke IP pemanggil.
    @PostMapping("/auth")
    public ResponseEntity<?> authenticate(
            @RequestHeader(value = "Authorization", defaultValue = "") String authHeader,
            HttpServletRequest request) {
        try {
            String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
            String callerIp = request.getRemoteAddr();
            SecureSession session = seimsService.authenticateSecure(token, callerIp);
            return ResponseEntity.ok(Map.of(
                    "authenticated", true,
                    "userId", session.getUserId(),
                    "boundIp", session.getBoundIpAddress()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }

        // === KODE RENTAN (SHALLOW MODEL) ===
        // Penjelasan: Memilah string tanpa melakukan kontrol keamanan apapun pada header Bearer Token.
        // Celah: Sembarang string non-kosong diterima, tidak ada identity binding.
        // String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        // boolean isValid = seimsService.authenticate(token);
        // this.currentSessionValid = isValid;
        // if (isValid) {
        //     return ResponseEntity.ok(Map.of("authenticated", true, "token", token, "userId", "anonymous"));
        // }
        // return ResponseEntity.status(401).body(Map.of("error", "Error: Not Autorized"));
    }

    // === DEEP MODEL - AKTIF ===
    // Semua endpoint data kini memerlukan token valid di setiap request.
    @GetMapping("/suppliers")
    public ResponseEntity<?> getAllSuppliers(
            @RequestHeader(value = "Authorization", defaultValue = "") String authHeader,
            HttpServletRequest request) {
        try {
            String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
            SecureSession session = seimsService.authenticateSecure(token, request.getRemoteAddr());
            var suppliers = seimsService.getAllSuppliers(session);
            return ResponseEntity.ok(Map.of("count", suppliers.size(), "data", suppliers));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }

        // === KODE RENTAN (SHALLOW MODEL) ===
        // try {
        //     List<Supplier> suppliers = seimsService.getAllSuppliers(currentSessionValid);
        //     return ResponseEntity.ok(Map.of("count", suppliers.size(), "data", suppliers));
        // } catch (SecurityException e) {
        //     return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        // }
    }

    @GetMapping("/suppliers/{id}")
    public ResponseEntity<?> getSupplierById(
            @PathVariable String id,
            @RequestHeader(value = "Authorization", defaultValue = "") String authHeader,
            HttpServletRequest request) {
        try {
            String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
            SecureSession session = seimsService.authenticateSecure(token, request.getRemoteAddr());
            var supplier = seimsService.getSupplierById(id, session);
            if (supplier == null) return ResponseEntity.status(404).body(Map.of("error", "Supplier not found"));
            return ResponseEntity.ok(Map.of("data", supplier));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }

        // === KODE RENTAN (SHALLOW MODEL) ===
        // Penjelasan: Enumerasi ID sekuensial tanpa pengecekan kepemilikan (IDOR).
        // try {
        //     Supplier supplier = seimsService.getSupplierById(id, currentSessionValid);
        //     ...
        // }
    }

    @GetMapping("/nda/{supplierId}")
    public ResponseEntity<?> getNdaBySupplier(
            @PathVariable String supplierId,
            @RequestHeader(value = "Authorization", defaultValue = "") String authHeader,
            HttpServletRequest request) {
        try {
            String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
            SecureSession session = seimsService.authenticateSecure(token, request.getRemoteAddr());
            var ndas = seimsService.getNdaBySupplier(supplierId, session);
            return ResponseEntity.ok(Map.of("count", ndas.size(), "data", ndas));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }

        // === KODE RENTAN (SHALLOW MODEL) ===
        // Penjelasan: Menghasilkan semua NDA tanpa pemilahan klasifikasi kerahasiaan.
        // try {
        //     List<SupplierNda> ndas = seimsService.getNdaBySupplier(supplierId, currentSessionValid);
        //     ...
        // }
    }
}
