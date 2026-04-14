package org.example.securecoding.intelbackend.controller;

import org.example.securecoding.intelbackend.service.ProductHierarchyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/product")
public class ProductHierarchyController {

    private final ProductHierarchyService productHierarchyService;

    // === KODE RENTAN (SHALLOW MODEL) ===
    // Penjelasan: Menyimpan state auth di memori controller, mensimulasikan nilai variabel manipulable di frontend.
    // Celah: Variabel boolean ini bisa dimanipulasi tanpa lewat proses autentikasi sesungguhnya.
    // private boolean currentSessionAuthenticated = false;

    public ProductHierarchyController(ProductHierarchyService productHierarchyService) {
        this.productHierarchyService = productHierarchyService;
    }

    // === DEEP MODEL - AKTIF ===
    // Login kini mengembalikan JWT yang berisi role dari DB.
    // Tidak ada lagi boolean session state yang tersimpan di memori controller.
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestParam String username,
            @RequestParam String password) {
        try {
            String jwt = productHierarchyService.authenticateAndIssueToken(username, password);
            return ResponseEntity.ok(Map.of("token", jwt, "username", username));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }

        // === KODE RENTAN (SHALLOW MODEL) ===
        // Penjelasan: Endpoint login memvalidasi input mentah dan mengatur state di tingkat kontroler.
        // Celah: boolean isAuth mudah dimanipulasi; tidak ada ikatan identitas di token.
        // boolean isAuth = productHierarchyService.authenticate(username, password);
        // this.currentSessionAuthenticated = isAuth;
        // if (isAuth) {
        //     return ResponseEntity.ok(Map.of("authenticated", true, "username", username, "role", "ADMIN"));
        // } else {
        //     return ResponseEntity.status(401).body(Map.of("authenticated", false, "error", "Error: Invalid Credentials"));
        // }
    }

    // === DEEP MODEL - AKTIF ===
    // Akses hierarki produk memerlukan JWT valid. Role ADMIN diverifikasi dari klaim token.
    @GetMapping("/hierarchy")
    public ResponseEntity<?> getProductHierarchy(
            @RequestParam String token) {
        try {
            var products = productHierarchyService.getProductsSecure(token);
            return ResponseEntity.ok(Map.of("count", products.size(), "data", products));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }

        // === KODE RENTAN (SHALLOW MODEL) ===
        // Penjelasan: Bypass mungkin terjadi jika attacker bisa memaksa atribut autentikasi diset manual.
        // Celah: currentSessionAuthenticated hanyalah boolean yang tidak terikat identitas apapun.
        // try {
        //     List<Product> products = productHierarchyService.getProducts(currentSessionAuthenticated);
        //     return ResponseEntity.ok(Map.of("count", products.size(), "data", products));
        // } catch (SecurityException e) {
        //     return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        // }
    }
}
