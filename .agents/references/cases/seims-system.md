# Kasus C: SEIMS (Supplier EHS IP Management System) — Analisis Sistem

## 1. System Overview

SEIMS (Supplier EHS IP Management System) adalah platform internal Intel yang digunakan untuk mengelola pertukaran Intellectual Property (IP) dengan pemasok (supplier) di bidang Environment, Health, and Safety (EHS). Sistem ini menyimpan data sensitif termasuk Non-Disclosure Agreements (NDA) dan informasi kontak karyawan yang berurusan dengan pemasok.

## 2. System Structure & Data Flow

*   **Frontend Framework:** Angular (Single Page Application)
*   **Authentication:** JWT-based authentication melalui endpoint `svc/auth`
*   **Session Management:** Bearer token di header `Authorization`
*   **Backend APIs:** Beberapa API endpoint untuk mengelola data supplier dan karyawan
*   **Employee IDs:** Bersifat sekuensial (sequential), tidak random/UUID

## 3. The Flaw (Vulnerability Analysis)

Sistem SEIMS memiliki tiga kerentanan desain utama yang saling memperkuat:

### 3.1 Broken JWT Validation (Session Mismanagement)

```java
// SHALLOW MODEL: Backend menerima string apapun sebagai Bearer token
public class InsecureSeimsSession {
    private String token;     // ← Tidak divalidasi secara kriptografis
    private String userId;    // ← Tidak terikat (bound) ke token

    public InsecureSeimsSession(String token) {
        // KERENTANAN: Tidak ada verifikasi JWT signature
        // Tidak ada pengecekan expiry
        // Tidak ada binding antara token dan userId
        this.token = token;
    }

    public boolean isValid() {
        // KERENTANAN KRITIS: Bahkan menerima string "Not Autorized" sebagai token valid!
        return token != null && !token.isEmpty();
    }
}
```

**Root Cause:** Backend **sama sekali tidak memvalidasi JWT secara kriptografis**. Server secara literal menerima string `"Not Autorized"` (dengan typo) sebagai Bearer token yang valid dan memberikan akses.

### 3.2 Missing Session Binding (No Identity Binding)

```java
// SHALLOW MODEL: Token tidak terikat ke identitas pengguna
public class InsecureSeimsSession {
    private String token;
    // TIDAK ADA: userId binding
    // TIDAK ADA: IP address binding
    // TIDAK ADA: expiry timestamp

    // Token bisa digunakan oleh SIAPA SAJA yang memilikinya
    // Tidak ada cara memverifikasi bahwa token ini milik pengguna tertentu
}
```

**Root Cause:** Token sesi dapat dimanipulasi atau direpresentasikan dalam objek sesi meskipun token tidak valid atau milik orang lain. Tidak ada **binding** antara token, userId, dan IP Address.

### 3.3 Insecure Direct Object Reference (IDOR) via Sequential IDs

```java
// SHALLOW MODEL: Employee IDs bersifat sekuensial
// Attacker bisa enumerasi semua karyawan:
for (int id = 1; id <= 10000; id++) {
    String response = httpClient.get("/api/employees/" + id);
    // Tidak ada Authorization header yang diperlukan!
    // Tidak ada rate limiting!
    saveToFile(response);
}
```

**Root Cause:** Kombinasi dari: (1) employee IDs yang sekuensial, (2) API yang tidak memerlukan Authorization header, dan (3) tidak adanya rate limiting memungkinkan enumerasi trivial seluruh database.

## 4. Exploit Mechanics

1. **Akses Situs:** Attacker mengakses situs SEIMS. Aplikasi mencoba fetch JWT dari backend dan gagal, mengembalikan error `"Not Autorized"` (dengan typo).
2. **Bypass Error Check:** Attacker memodifikasi kode frontend Angular untuk mengabaikan string error spesifik ini.
3. **Backend Menerima Token Palsu:** Secara mengejutkan, backend **menerima** string `"Not Autorized"` sebagai Bearer token yang valid dan memberikan akses ke sistem.
4. **Override Auth API:** Attacker memodifikasi response dari `svc/auth` API untuk set `IsAuth=true`.
5. **Discover Sensitive APIs:** Attacker menemukan API endpoint yang mengekspos data karyawan dan NDA supplier.
6. **Enumerasi Sekuensial:** Karena employee IDs bersifat sekuensial dan tidak ada Authorization header yang diperlukan, attacker menulis script sederhana untuk mengiterasi semua ID dan mengunduh seluruh database.

## 5. Security Impact

*   **Data Karyawan Terekspos:** Detail semua karyawan Intel yang terdaftar di sistem SEIMS.
*   **NDA Supplier Bocor:** Non-Disclosure Agreements yang bersifat rahasia antara Intel dan pemasoknya.
*   **IP Information:** Informasi terkait Intellectual Property terkait Environment, Health, and Safety.
*   **Enumeration Trivial:** Seluruh database bisa di-scrape dengan script sederhana tanpa otentikasi.

## 6. Deep Model Fix (Rencana Perbaikan)

| Kerentanan | Domain Primitive / Perbaikan |
|---|---|
| Broken JWT validation | `SecureSession` — JWT divalidasi secara kriptografis (signature + claims) di server |
| Missing identity binding | `SecureSession` konstruktor wajib menerima `UserId + Token + IpAddress` — jika tidak cocok, **Exception** |
| No expiry | `SecureSession` harus memiliki `Instant expiresAt` — objek mustahil dibuat jika sudah expired |
| Sequential ID enumeration | UUID sebagai identifier publik, bukan sequential BIGINT |
| Missing Authorization header | Setiap API endpoint wajib `@PreAuthorize` atau filter token |
| No rate limiting | Rate limiter (e.g., Bucket4j) untuk mencegah enumerasi |

### Contoh Deep Model (Rencana)

```java
// DEEP MODEL: SecureSession dengan invariant ketat
public final class SecureSession {
    private final String userId;
    private final String token;
    private final String boundIpAddress;
    private final Instant expiresAt;

    // INVARIANT: Semua binding WAJIB valid saat konstruksi
    public SecureSession(String userId, String token, String ipAddress, Instant expiresAt) {
        // 1. Non-null checks
        Objects.requireNonNull(userId, "userId wajib ada");
        Objects.requireNonNull(token, "token wajib ada");
        Objects.requireNonNull(ipAddress, "IP address wajib ada");
        Objects.requireNonNull(expiresAt, "expiry wajib ada");

        // 2. Token harus valid secara kriptografis
        if (!JwtValidator.isValidSignature(token)) {
            throw new IllegalArgumentException("Token JWT tidak valid secara kriptografis");
        }

        // 3. Token harus milik userId ini
        String tokenUserId = JwtValidator.extractUserId(token);
        if (!userId.equals(tokenUserId)) {
            throw new IllegalArgumentException("Token tidak terikat ke userId ini");
        }

        // 4. Belum expired
        if (expiresAt.isBefore(Instant.now())) {
            throw new IllegalArgumentException("Session sudah expired");
        }

        // 5. IP harus valid
        if (!IpAddress.isValid(ipAddress)) {
            throw new IllegalArgumentException("IP Address tidak valid");
        }

        this.userId = userId;
        this.token = token;
        this.boundIpAddress = ipAddress;
        this.expiresAt = expiresAt;
    }

    // TIDAK ADA SETTER — immutable, invalid state unrepresentable
}
```

## 7. Planned Package Structure

```
src/main/java/org/example/securecoding/intelbackend/
├── shallow/
│   ├── InsecureSeimsSession.java           # No JWT validation, no binding
│   ├── InsecureSeimsAPI.java               # Sequential IDs, no auth header required
│   └── SeimsShallowController.java
└── deep/
    ├── domain/
    │   ├── SecureSession.java              # Immutable: UserId + Token + IP + Expiry
    │   ├── IpAddress.java                  # Domain Primitive for IP validation
    │   └── EmployeePublicId.java           # UUID-based public identifier
    ├── SecureSeimsAPI.java
    └── SeimsDeepController.java
```
