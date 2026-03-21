# Kasus B: Product Hierarchy System — Analisis Sistem

## 1. System Overview

"Hierarchy Management" dan "Product Onboarding" adalah dua situs web internal Intel yang berhubungan langsung. Platform Hierarchy Management digunakan untuk mengelola hierarki produk Intel, sedangkan Product Onboarding berfungsi sebagai portal untuk memperkenalkan produk baru ke dalam ekosistem Intel. Kedua situs ini dibangun di atas ReactJS dan berbagi pola autentikasi yang identik — serta kelemahan keamanan yang sama.

## 2. System Structure & Data Flow

*   **Frontend Framework:** ReactJS (Single Page Application)
*   **Authentication:** Microsoft Azure SSO melalui Graph API untuk mengambil peran pengguna
*   **Authorization Model:** Peran dikelola sebagai `String[] roles` di sisi klien, termasuk string peran seperti `"SPARK Product Management System Admin"`
*   **Backend:** API server yang mengandalkan frontend untuk memvalidasi status autentikasi
*   **Secrets Management:** Kredensial administratif, password plaintext, dan Basic Auth headers di-hardcode langsung di dalam JavaScript klien

## 3. The Flaw (Vulnerability Analysis)

Sistem ini menderita tiga kategori kerentanan desain yang saling tumpang tindih:

### 3.1 Boolean Override & Role Spoofing (Primitive Obsession)

```java
// SHALLOW MODEL: Status login adalah boolean primitif yang bisa di-override
public class InsecureProductSession {
    private boolean isAuthenticated = false;  // ← Attacker sets to true
    private String[] roles = {};              // ← Attacker hardcodes ["admin"]

    // KERENTANAN: Setter terbuka tanpa validasi
    public void setAuthenticated(boolean auth) {
        this.isAuthenticated = auth;  // Tidak ada challenge, tidak ada verifikasi
    }

    public void setRoles(String[] roles) {
        this.roles = roles;  // Peran bisa diatur langsung dari klien
    }
}
```

**Root Cause:** Status `isAuthenticated` hanyalah boolean primitif (`Primitive Obsession`). Tidak ada transisi state yang tervalidasi — attacker cukup mengubah variabel JavaScript lokal dari `false` ke `true`.

### 3.2 Hardcoded Credentials & Plaintext Secrets

```javascript
// Ditemukan di source code JavaScript klien:
const ADMIN_USERNAME = "product-admin";
const ADMIN_PASSWORD = "IntelP@ssw0rd2024!";  // ← Plaintext!
const BASIC_AUTH = "Basic cHJvZHVjdC1hZG1pbjpJbnRlbFBAc3N3MHJkMjAyNCE=";

// GitHub personal access token di dalam komentar kode:
// TODO: move to env - ghp_1a2b3c4d5e6f7g8h9i0j...
```

**Root Cause:** Pengembang mengambil jalan pintas untuk mempercepat pengembangan internal, **tanpa mempertimbangkan bahwa situs internal tetap bisa ditemukan** oleh penyerang.

### 3.3 "Pointless" Client-Side Encryption

```javascript
// Pengembang mencoba mengenkripsi password menggunakan AES...
const encryptedPassword = AES.encrypt(password, secretKey);
// ...tapi kunci dekripsi dikirimkan ke browser!
const secretKey = "MyS3cur3K3y!!";
// Bahkan meninggalkan komentar: "Decrypt at https://www.aesencryptor.com/"
```

**Root Cause:** Enkripsi AES di sisi klien memberikan **nol keamanan** karena kunci dekripsi secara inheren dikirimkan bersama kode ke browser pengguna.

## 4. Exploit Mechanics

1. **Skip SSO Redirect:** Attacker mengubah variabel `isAuthenticated` menjadi `true` di startup kode ReactJS, melewati redirect SSO.
2. **Hentikan Graph API Call:** Attacker meng-comment-out panggilan Microsoft Graph API yang mengambil peran pengguna, menjadikannya "no-op".
3. **Hardcode Role Admin:** Attacker mengganti array peran lokal dengan `["SPARK Product Management System Admin"]` atau `["admin"]`.
4. **Akses Penuh:** Aplikasi memberikan akses administratif penuh tanpa pernah memverifikasi identitas di sisi server.
5. **Eks-filter Kredensial:** Attacker mengekstrak plaintext credentials dan GitHub token dari source code klien.

## 5. Security Impact

*   **Administrative Access:** Attacker mendapatkan akses admin penuh ke platform Product Hierarchy dan Product Onboarding.
*   **Exposed Secrets:** Plaintext passwords, Basic Auth headers, dan GitHub personal access token terekspos.
*   **Supply Chain Risk:** GitHub token yang bocor berpotensi digunakan untuk membuat produk palsu (rogue products) di database Intel ARK publik.
*   **Credential Reuse:** Password plaintext yang ditemukan berpotensi digunakan untuk serangan credential stuffing terhadap sistem Intel lainnya.

## 6. Deep Model Fix (Rencana Perbaikan)

| Kerentanan | Domain Primitive / Perbaikan |
|---|---|
| Boolean `isAuthenticated` flag | `AuthenticationState` enum (`UNAUTHENTICATED`, `PENDING_VERIFICATION`, `AUTHENTICATED`) — **tanpa setter publik**, hanya transisi via method tervalidasi |
| `String[] roles` (Primitive Obsession) | `Role` enum (`USER`, `ADMIN`) — didefinisikan di server, tidak bisa dimanipulasi klien |
| Hardcoded credentials | Backend Secret Vault — semua kredensial dipindahkan ke environment variables / Spring `@Value` |
| Client-side AES encryption | Dihapus total — semua operasi kriptografi dilakukan di sisi server |
| Open setter untuk auth state | `VerifiedAuthSession` — immutable, hanya bisa diciptakan melalui `VerifiedAuthSession.create(credentials)` yang memvalidasi secara kriptografis |

### Contoh Deep Model (Rencana)

```java
// DEEP MODEL: Role sebagai enum, bukan String
public enum Role {
    USER, ADMIN;

    // Invariant: Role hanya bisa ditentukan oleh server
    public static Role fromServerToken(String jwtClaim) {
        if ("ADMIN".equals(jwtClaim)) return ADMIN;
        return USER;  // Default ke privilege terendah
    }
}

// DEEP MODEL: Auth state yang immutable
public final class VerifiedAuthSession {
    private final String userId;
    private final Role role;
    private final Instant expiresAt;

    // Private constructor — hanya bisa dibuat via factory method
    private VerifiedAuthSession(String userId, Role role, Instant expiresAt) {
        // INVARIANT: Semua field wajib ada
        Objects.requireNonNull(userId, "userId tidak boleh null");
        Objects.requireNonNull(role, "role tidak boleh null");
        if (expiresAt.isBefore(Instant.now())) {
            throw new IllegalArgumentException("Session sudah expired");
        }
        this.userId = userId;
        this.role = role;
        this.expiresAt = expiresAt;
    }

    // Factory method: satu-satunya jalan membuat session yang valid
    public static VerifiedAuthSession fromValidatedToken(String jwt) {
        // Validasi JWT secara kriptografis di sisi server
        // Parse claims, extract userId dan role
        // Throw exception jika invalid
        ...
    }

    // TIDAK ADA SETTER — immutable by design
}
```

## 7. Planned Package Structure

```
src/main/java/org/example/securecoding/intelbackend/
├── shallow/
│   ├── InsecureProductSession.java      # Boolean auth + String[] roles
│   ├── InsecureProductHierarchyAPI.java  # Hardcoded credentials, no server validation
│   └── ProductHierarchyShallowController.java
└── deep/
    ├── domain/
    │   ├── Role.java                    # Enum: USER, ADMIN
    │   └── VerifiedAuthSession.java     # Immutable, validated at creation
    ├── SecureProductHierarchyAPI.java
    └── ProductHierarchyDeepController.java
```
