package org.example.securecoding.intelbackend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.securecoding.intelbackend.domain.EmailAddress;
import org.example.securecoding.intelbackend.domain.PhoneNumber;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Employee {

    @Id
    private String id;

    private String fullName;
    private String role;
    private String manager;

    // DEEP MODEL: Menggunakan Domain Primitives bukannya String mentah
    private EmailAddress email;
    private PhoneNumber phoneNumber;

    public Employee(String fullName, String role, String manager, EmailAddress email, PhoneNumber phoneNumber) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee name cannot be empty (Deep Model Violation)");
        }
        this.fullName = fullName;
        this.role = role;
        this.manager = manager;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    /**
     * === KODE RENTAN (SHALLOW MODEL) — Dipertahankan untuk Komparatif ===
     *
     * @Entity
     * @Data
     * @NoArgsConstructor
     * @AllArgsConstructor
     * public class Employee {
     *     private Long id;
     *     private String fullName;
     *     private String role;
     *     private String manager;
     *     private String email;          // Primitive Obsession: String tidak mencegah format salah
     *     private String phoneNumber;    // Primitive Obsession: String rentan terhadap malformed data
     * }
     */
}
