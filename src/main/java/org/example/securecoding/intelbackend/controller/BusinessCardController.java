package org.example.securecoding.intelbackend.controller;

import org.example.securecoding.intelbackend.service.BusinessCardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/business-card")
public class BusinessCardController {

    private final BusinessCardService businessCardService;

    public BusinessCardController(BusinessCardService businessCardService) {
        this.businessCardService = businessCardService;
    }

    // === DEEP MODEL - AKTIF ===
    // Endpoint ini sekarang memerlukan userId dan role secara eksplisit.
    @PostMapping("/token")
    public ResponseEntity<?> getAccessToken(
            @RequestParam String userId,
            @RequestParam(defaultValue = "USER") String role) {
        try {
            String token = businessCardService.generateAuthenticatedToken(userId, role);
            return ResponseEntity.ok(Map.of("accessToken", token, "userId", userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }

        // === KODE RENTAN (SHALLOW MODEL) ===
        // Penjelasan: Endpoint menerima permintaan tanpa proses login terlebih dahulu.
        // Celah: generateAnonymousToken() langsung mengembalikan privileged token tanpa
        // autentikasi.
        // String token = businessCardService.generateAnonymousToken();
        // return ResponseEntity.ok(Map.of("accessToken", token));
    }

    // === DEEP MODEL - AKTIF ===
    // Pencarian karyawan memerlukan JWT valid DAN query tidak kosong.
    @GetMapping("/employees")
    public ResponseEntity<?> getEmployeeData(
            @RequestParam String token,
            @RequestParam String userId,
            @RequestParam String search) {
        try {
            var employees = businessCardService.searchEmployeesSecure(token, userId, search);
            return ResponseEntity.ok(Map.of("count", employees.size(), "data", employees));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }

        // === KODE RENTAN (SHALLOW MODEL) ===
        // Penjelasan: Menjalankan eksekusi pencarian yang tidak divalidasi, membiarkan
        // data over-fetching.
        // try {
        // List<Employee> employees = businessCardService.getEmployeeData(token,
        // search);
        // return ResponseEntity.ok(Map.of("count", employees.size(), "data",
        // employees));
        // } catch (SecurityException e) {
        // return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        // }
    }
}
