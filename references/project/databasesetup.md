# Database Setup & Data Population Guide

## 1. Konteks
Dalam fase proyek "Secure Coding" ini, kita menggunakan in-memory **H2 Database** di dalam backend Java Spring Boot. Tujuannya adalah mensimulasikan data internal Intel yang terekspos selama insiden "Intel Outside" untuk **ketiga studi kasus**:

| Kasus | Tabel | Data yang Disimulasikan |
|---|---|---|
| **A** — Business Card Portal | `EMPLOYEE` | 270.000 employee records (disimulasikan 151 records) |
| **B** — Product Hierarchy | `PRODUCT`, `ADMIN_CREDENTIAL` | Hierarki produk Intel + hardcoded admin credentials |
| **C** — SEIMS | `SUPPLIER`, `SUPPLIER_NDA`, `USER_SESSION` | Data supplier, NDA rahasia, sesi pengguna |

---

## 2. Application Configuration (`application.properties`)

File: `src/main/resources/application.properties`

```properties
spring.application.name=intel-backend

# H2 Database Configuration
spring.datasource.url=jdbc:h2:mem:inteldb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password

# Enable H2 Console at http://localhost:8080/h2-console
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# Hibernate/JPA Configuration
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=none

# Auto-run schema.sql and data.sql on startup
spring.sql.init.mode=always
```

> **Catatan:** `ddl-auto` disetel ke `none` karena schema dibuat secara manual melalui `schema.sql`, bukan melalui auto-generate Hibernate.

---

## 3. Database Schema (`schema.sql`)

File: `src/main/resources/schema.sql`

### Kasus A — Employee Table (✅ Sudah Diimplementasikan)

```sql
-- =============================================================================
-- KASUS A: Business Card Portal
-- Simulates Intel's global employee directory (270,000 records in real breach)
-- =============================================================================
CREATE TABLE IF NOT EXISTS EMPLOYEE (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name    VARCHAR(255),
    role         VARCHAR(255),
    manager      VARCHAR(255),
    email        VARCHAR(255),
    phone_number VARCHAR(50)
);
```

### Kasus B — Product Hierarchy Tables (✅ Sudah Diimplementasikan)

```sql
-- =============================================================================
-- KASUS B: Product Hierarchy System
-- Simulates Intel's product hierarchy management + hardcoded admin credentials
-- =============================================================================

-- Tabel produk: hierarki produk Intel (ARK database)
CREATE TABLE IF NOT EXISTS PRODUCT (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_name    VARCHAR(255),
    product_code    VARCHAR(100),
    category        VARCHAR(100),       -- e.g., 'Processors', 'Chipsets', 'NUC'
    status          VARCHAR(50),        -- e.g., 'Launched', 'Announced', 'Discontinued'
    parent_id       BIGINT,             -- Self-referencing FK for hierarchy
    created_by      VARCHAR(100),
    FOREIGN KEY (parent_id) REFERENCES PRODUCT(id)
);

-- Tabel kredensial admin: SENGAJA INSECURE (Shallow Model)
-- Dalam sistem asli, credentials hardcoded di JavaScript klien
CREATE TABLE IF NOT EXISTS ADMIN_CREDENTIAL (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(100),
    password        VARCHAR(255),       -- SHALLOW: Plaintext! DEEP: Hashed
    role            VARCHAR(100),       -- SHALLOW: String "admin". DEEP: Enum
    github_token    VARCHAR(255),       -- SHALLOW: Exposed PAT. DEEP: Removed
    basic_auth      VARCHAR(255)        -- SHALLOW: Plaintext Base64. DEEP: Removed
);
```

### Kasus C — SEIMS Tables (✅ Sudah Diimplementasikan)

```sql
-- =============================================================================
-- KASUS C: SEIMS (Supplier EHS IP Management System)
-- Simulates supplier data, NDAs, and session management
-- =============================================================================

-- Tabel supplier: data pemasok Intel
CREATE TABLE IF NOT EXISTS SUPPLIER (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,  -- SHALLOW: Sequential (enumerable)
    company_name    VARCHAR(255),
    contact_person  VARCHAR(255),
    email           VARCHAR(255),
    phone           VARCHAR(50),
    country         VARCHAR(100),
    ehs_status      VARCHAR(50)         -- e.g., 'Compliant', 'Pending', 'Non-Compliant'
);

-- Tabel NDA: Non-Disclosure Agreements (data sangat sensitif)
CREATE TABLE IF NOT EXISTS SUPPLIER_NDA (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    supplier_id     BIGINT,
    nda_title       VARCHAR(255),
    signed_date     DATE,
    expiry_date     DATE,
    classification  VARCHAR(50),        -- e.g., 'Confidential', 'Top Secret'
    document_url    VARCHAR(500),       -- Link ke dokumen NDA
    FOREIGN KEY (supplier_id) REFERENCES SUPPLIER(id)
);

-- Tabel sesi: user session management (SHALLOW: no binding)
CREATE TABLE IF NOT EXISTS USER_SESSION (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         VARCHAR(100),       -- SHALLOW: Tidak terikat ke token
    token           VARCHAR(500),       -- SHALLOW: Tidak divalidasi. DEEP: JWT verified
    ip_address      VARCHAR(50),        -- SHALLOW: Tidak dicek. DEEP: Bound to session
    created_at      TIMESTAMP,
    expires_at      TIMESTAMP           -- SHALLOW: Tidak ada/diabaikan. DEEP: Enforced
);
```

---

## 4. Entity Models

### Kasus A — `Employee.java` (✅ Sudah Diimplementasikan)

File: `src/main/java/org/example/securecoding/intelbackend/model/Employee.java`

```java
package org.example.securecoding.intelbackend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private String role;
    private String manager;
    private String email;
    private String phoneNumber;
}
```

### Kasus B — `Product.java` & `AdminCredential.java` (✅ Sudah Diimplementasikan)

```java
package org.example.securecoding.intelbackend.model;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String productName;
    private String productCode;
    private String category;
    private String status;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Product parent;     // Self-referencing for hierarchy

    private String createdBy;
}
```

```java
package org.example.securecoding.intelbackend.model;

// SHALLOW MODEL: Credentials stored in database with plaintext password
// In the real breach, these were hardcoded in client-side JavaScript
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;       // VULNERABILITY: Plaintext — not hashed
    private String role;           // VULNERABILITY: String, not Enum
    private String githubToken;    // VULNERABILITY: Exposed PAT in code
    private String basicAuth;      // VULNERABILITY: Plaintext Base64
}
```

### Kasus C — `Supplier.java`, `SupplierNda.java` (✅ Sudah Diimplementasikan)

```java
package org.example.securecoding.intelbackend.model;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;               // VULNERABILITY: Sequential, enumerable

    private String companyName;
    private String contactPerson;
    private String email;
    private String phone;
    private String country;
    private String ehsStatus;
}
```

```java
package org.example.securecoding.intelbackend.model;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierNda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    private String ndaTitle;
    private LocalDate signedDate;
    private LocalDate expiryDate;
    private String classification;  // e.g., 'Confidential', 'Top Secret'
    private String documentUrl;
}
```

---

## 5. Repository Interfaces

### Kasus A — `EmployeeRepository.java` (✅ Sudah Diimplementasikan)

```java
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByFullNameContainingIgnoreCase(String name);
}
```

### Kasus B — Repositories (✅ Sudah Diimplementasikan)

```java
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory(String category);
    List<Product> findByStatus(String status);
    List<Product> findByParentIsNull();  // Root-level products
}

@Repository
public interface AdminCredentialRepository extends JpaRepository<AdminCredential, Long> {
    Optional<AdminCredential> findByUsername(String username);
}
```

### Kasus C — Repositories (✅ Sudah Diimplementasikan)

```java
@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    List<Supplier> findByCompanyNameContainingIgnoreCase(String name);
    List<Supplier> findByCountry(String country);
}

@Repository
public interface SupplierNdaRepository extends JpaRepository<SupplierNda, Long> {
    List<SupplierNda> findBySupplierId(Long supplierId);
}
```

---

## 6. Seed Data (`data.sql`)

File: `src/main/resources/data.sql`

Spring Boot otomatis mengeksekusi `data.sql` setiap kali server start (`spring.sql.init.mode=always`).

### Kasus A — Employee Records (✅ 151 records sudah ada)

```sql
-- Target utama: security researcher dari insiden asli
INSERT INTO EMPLOYEE (full_name, role, manager, email, phone_number)
VALUES ('Eaton Zveare', 'Security Researcher', 'Jane Doe', 'eaton@intel.com', '+1-555-0000');

-- + 150 dummy records karyawan global
```

### Kasus B — Products & Admin Credentials (✅ Sudah Ada)

```sql
-- =============================================================================
-- KASUS B: Product Hierarchy + Hardcoded Credentials
-- =============================================================================

-- Product hierarchy (root categories)
INSERT INTO PRODUCT (product_name, product_code, category, status, parent_id, created_by) VALUES ('Intel Processors', 'PROC-ROOT', 'Processors', 'Launched', NULL, 'admin');
INSERT INTO PRODUCT (product_name, product_code, category, status, parent_id, created_by) VALUES ('Intel Chipsets', 'CHIP-ROOT', 'Chipsets', 'Launched', NULL, 'admin');
INSERT INTO PRODUCT (product_name, product_code, category, status, parent_id, created_by) VALUES ('Intel NUC', 'NUC-ROOT', 'NUC', 'Launched', NULL, 'admin');

-- Child products (under Processors, id=1)
INSERT INTO PRODUCT (product_name, product_code, category, status, parent_id, created_by) VALUES ('Core i9-14900K', 'PROC-14900K', 'Processors', 'Launched', 1, 'admin');
INSERT INTO PRODUCT (product_name, product_code, category, status, parent_id, created_by) VALUES ('Core i7-14700K', 'PROC-14700K', 'Processors', 'Launched', 1, 'admin');
INSERT INTO PRODUCT (product_name, product_code, category, status, parent_id, created_by) VALUES ('Core Ultra 200S', 'PROC-ULTRA200', 'Processors', 'Announced', 1, 'admin');
INSERT INTO PRODUCT (product_name, product_code, category, status, parent_id, created_by) VALUES ('Xeon w9-3595X', 'XEON-3595X', 'Processors', 'Launched', 1, 'admin');

-- VULNERABILITY: Hardcoded admin credentials (in real breach, these were in JS source code)
INSERT INTO ADMIN_CREDENTIAL (username, password, role, github_token, basic_auth) VALUES ('product-admin', 'IntelP@ssw0rd2024!', 'SPARK Product Management System Admin', 'ghp_1a2b3c4d5e6f7g8h9i0jklmnopqrstuv', 'Basic cHJvZHVjdC1hZG1pbjpJbnRlbFBAc3N3MHJkMjAyNCE=');
INSERT INTO ADMIN_CREDENTIAL (username, password, role, github_token, basic_auth) VALUES ('hierarchy-viewer', 'ViewerPass123', 'viewer', NULL, 'Basic aGllcmFyY2h5LXZpZXdlcjpWaWV3ZXJQYXNzMTIz');
```

### Kasus C — Suppliers & NDAs (✅ Sudah Ada)

```sql
-- =============================================================================
-- KASUS C: SEIMS — Suppliers & NDAs
-- =============================================================================

-- Supplier data (sequential IDs — vulnerable to enumeration)
INSERT INTO SUPPLIER (company_name, contact_person, email, phone, country, ehs_status) VALUES ('Taiwan Semiconductor Manufacturing', 'Wei-Lin Chang', 'wchang@tsmc.com', '+886-3-5636688', 'Taiwan', 'Compliant');
INSERT INTO SUPPLIER (company_name, contact_person, email, phone, country, ehs_status) VALUES ('Samsung Electronics', 'Min-Soo Park', 'mspark@samsung.com', '+82-2-2255-0114', 'South Korea', 'Compliant');
INSERT INTO SUPPLIER (company_name, contact_person, email, phone, country, ehs_status) VALUES ('SK Hynix', 'Ji-Hoon Lee', 'jhlee@skhynix.com', '+82-2-3459-2114', 'South Korea', 'Pending');
INSERT INTO SUPPLIER (company_name, contact_person, email, phone, country, ehs_status) VALUES ('Micron Technology', 'Robert Ellison', 'rellison@micron.com', '+1-208-368-4000', 'USA', 'Compliant');
INSERT INTO SUPPLIER (company_name, contact_person, email, phone, country, ehs_status) VALUES ('ASE Group', 'Hsiao-Wen Liu', 'hwliu@aseglobal.com', '+886-2-8780-5489', 'Taiwan', 'Non-Compliant');
INSERT INTO SUPPLIER (company_name, contact_person, email, phone, country, ehs_status) VALUES ('GlobalFoundries', 'Ahmed Nassar', 'anassar@gf.com', '+1-518-305-7200', 'USA', 'Compliant');
INSERT INTO SUPPLIER (company_name, contact_person, email, phone, country, ehs_status) VALUES ('Infineon Technologies', 'Klaus Wagner', 'kwagner@infineon.com', '+49-89-234-0', 'Germany', 'Compliant');
INSERT INTO SUPPLIER (company_name, contact_person, email, phone, country, ehs_status) VALUES ('Nanya Technology', 'Yi-Chen Wu', 'ycwu@nanya.com', '+886-3-328-1688', 'Taiwan', 'Pending');
INSERT INTO SUPPLIER (company_name, contact_person, email, phone, country, ehs_status) VALUES ('United Microelectronics Corp', 'Cheng-Yu Tan', 'cytan@umc.com', '+886-3-578-2258', 'Taiwan', 'Compliant');
INSERT INTO SUPPLIER (company_name, contact_person, email, phone, country, ehs_status) VALUES ('Amkor Technology', 'Sujit Banerjee', 'sbanerjee@amkor.com', '+1-480-821-5000', 'USA', 'Compliant');

-- Confidential NDAs (sensitive documents exposed in the real breach)
INSERT INTO SUPPLIER_NDA (supplier_id, nda_title, signed_date, expiry_date, classification, document_url) VALUES (1, 'TSMC 3nm Process Node IP Agreement', '2023-01-15', '2028-01-15', 'Top Secret', '/docs/nda/tsmc-3nm-ip-2023.pdf');
INSERT INTO SUPPLIER_NDA (supplier_id, nda_title, signed_date, expiry_date, classification, document_url) VALUES (1, 'TSMC CoWoS Packaging Agreement', '2023-06-01', '2026-06-01', 'Confidential', '/docs/nda/tsmc-cowos-2023.pdf');
INSERT INTO SUPPLIER_NDA (supplier_id, nda_title, signed_date, expiry_date, classification, document_url) VALUES (2, 'Samsung HBM4 Supply Agreement', '2024-03-10', '2027-03-10', 'Top Secret', '/docs/nda/samsung-hbm4-2024.pdf');
INSERT INTO SUPPLIER_NDA (supplier_id, nda_title, signed_date, expiry_date, classification, document_url) VALUES (3, 'SK Hynix DDR6 Development Partnership', '2024-01-20', '2029-01-20', 'Confidential', '/docs/nda/skhynix-ddr6-2024.pdf');
INSERT INTO SUPPLIER_NDA (supplier_id, nda_title, signed_date, expiry_date, classification, document_url) VALUES (4, 'Micron CXL Memory Module Agreement', '2023-09-05', '2026-09-05', 'Confidential', '/docs/nda/micron-cxl-2023.pdf');
INSERT INTO SUPPLIER_NDA (supplier_id, nda_title, signed_date, expiry_date, classification, document_url) VALUES (7, 'Infineon Power IC Specifications', '2024-02-14', '2027-02-14', 'Top Secret', '/docs/nda/infineon-power-2024.pdf');

-- Session data (SHALLOW: no binding, no validation)
INSERT INTO USER_SESSION (user_id, token, ip_address, created_at, expires_at) VALUES ('user001', 'Not Autorized', '192.168.1.100', '2025-01-01 08:00:00', '2025-01-01 20:00:00');
INSERT INTO USER_SESSION (user_id, token, ip_address, created_at, expires_at) VALUES ('user002', 'eyJhbGciOiJub25lIn0.eyJ1c2VyIjoiZmFrZSJ9.', '10.0.0.50', '2025-01-01 09:00:00', '2025-01-01 21:00:00');
```

---

## 7. Integrasi dengan Shallow Model (Kerentanan per Kasus)

### Kasus A — Data Over-fetching
Setelah H2 database ter-populate, `InsecureBusinessCardAPI` me-query `EMPLOYEE` table:
- Search valid (`"Eaton"`) → **1 record**
- Empty search (`""`) → **`findAll()` — dump seluruh 151 records** (data breach!)

```java
// Dalam InsecureBusinessCardAPI.java:
if (searchFilter == null || searchFilter.trim().isEmpty()) {
    // DISASTER: Returns ALL employee records — the data breach!
    return employeeRepository.findAll();
}
```

### Kasus B — Hardcoded Credentials Exposure
`InsecureProductHierarchyAPI` akan me-query `ADMIN_CREDENTIAL` table:
- Login dengan plaintext password → akses admin
- Credentials juga bisa ditemukan di client-side source code (disimulasikan oleh tabel)

```java
// Dalam InsecureProductHierarchyAPI.java:
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
    // ...
}
```

### Kasus C — Sequential ID Enumeration + Broken Session
`InsecureSeimsAPI` akan me-query `SUPPLIER` dan `SUPPLIER_NDA`:
- Sequential IDs → Attacker enumerate `/api/supplier/1`, `/api/supplier/2`, ...
- Token `"Not Autorized"` diterima sebagai valid Bearer token
- NDA dokumen rahasia terekspos tanpa otorisasi

```java
// Dalam InsecureSeimsAPI.java:
public Supplier getSupplierById(Long id, InsecureSeimsSession session) {
    // VULNERABILITY: Only checks boolean validity — no real auth
    if (!session.isValid()) {
        throw new SecurityException("Error: Not Autorized");  // Typo matches real breach
    }
    // VULNERABILITY: Sequential ID allows trivial enumeration
    return supplierRepository.findById(id).orElse(null);
}
```

---

## 8. H2 Console — Verifikasi Semua Tabel

URL: `http://localhost:8080/h2-console`
JDBC URL: `jdbc:h2:mem:inteldb` | Username: `sa` | Password: `password`

```sql
-- Kasus A: Count employees
SELECT COUNT(*) FROM EMPLOYEE;                    -- Expect: 151

-- Kasus B: View products & credentials
SELECT * FROM PRODUCT;
SELECT * FROM ADMIN_CREDENTIAL;                   -- Lihat plaintext passwords!

-- Kasus C: Enumerate suppliers & view NDAs
SELECT * FROM SUPPLIER;                           -- Sequential IDs
SELECT * FROM SUPPLIER_NDA WHERE classification = 'Top Secret';  -- Sensitive!
SELECT * FROM USER_SESSION;                       -- Broken tokens
```