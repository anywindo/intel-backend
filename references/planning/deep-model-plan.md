# Deep Model Implementation Plan — Secure by Design

## 1. Prinsip Utama

Semua perbaikan Deep Model mengikuti prinsip **Domain-Driven Design (DDD) untuk Keamanan**:

1. **Make Invalid State Unrepresentable** — State berbahaya mustahil direpresentasikan oleh kode
2. **Domain Primitives** — Bungkus tipe primitif menjadi kelas khusus dengan invariant di konstruktor
3. **Immutability** — Objek domain tidak memiliki setter publik; state hanya bisa diatur saat konstruksi
4. **Invariant Enforcement** — Validasi ketat di konstruktor; throw `IllegalArgumentException` jika tidak valid
5. **Server-Side Only** — Semua keputusan keamanan terjadi di backend, bukan di frontend

---

## 2. Kasus A — Business Card Portal (Deep Model)

### Domain Primitives

#### `VerifiedSession`
```java
public final class VerifiedSession {
    private final String userId;
    private final String token;
    private final Instant expiresAt;

    public VerifiedSession(String userId, String jwt) {
        Objects.requireNonNull(userId);
        Objects.requireNonNull(jwt);
        // Validasi JWT secara kriptografis
        if (!JwtValidator.verify(jwt)) throw new IllegalArgumentException("Invalid JWT");
        // Binding: token harus milik userId ini
        if (!JwtValidator.extractSub(jwt).equals(userId)) throw new IllegalArgumentException("Token mismatch");
        this.userId = userId;
        this.token = jwt;
        this.expiresAt = JwtValidator.extractExpiry(jwt);
    }
    // NO SETTERS — immutable
}
```

#### `EmployeeSearchQuery`
```java
public final class EmployeeSearchQuery {
    private final String value;

    public EmployeeSearchQuery(String raw) {
        // INVARIANT: Query tidak boleh null, kosong, atau terlalu pendek
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException("Search query tidak boleh kosong");
        }
        if (raw.trim().length() < 2) {
            throw new IllegalArgumentException("Search query minimal 2 karakter");
        }
        this.value = raw.trim();
    }
    // NO SETTERS — immutable
}
```

### Secure Service
```java
public class SecureBusinessCardAPI {
    public List<Employee> searchEmployees(VerifiedSession session, EmployeeSearchQuery query) {
        // Session sudah tervalidasi di konstruktor — jika kita sampai sini, pasti valid
        // Query sudah tervalidasi — mustahil kosong
        return repository.findByFullNameContainingIgnoreCase(query.getValue());
        // findAll() TIDAK PERNAH dipanggil — invalid state unrepresentable
    }
}
```

---

## 3. Kasus B — Product Hierarchy (Deep Model)

### Domain Primitives

#### `Role` (Enum)
```java
public enum Role {
    USER, ADMIN;

    public static Role fromServerToken(String jwtRoleClaim) {
        if ("ADMIN".equalsIgnoreCase(jwtRoleClaim)) return ADMIN;
        return USER; // Default ke privilege terendah (Principle of Least Privilege)
    }
}
```

#### `VerifiedAuthSession`
```java
public final class VerifiedAuthSession {
    private final String userId;
    private final Role role;
    private final Instant expiresAt;

    private VerifiedAuthSession(String userId, Role role, Instant expiresAt) {
        Objects.requireNonNull(userId);
        Objects.requireNonNull(role);
        if (expiresAt.isBefore(Instant.now())) throw new IllegalArgumentException("Expired");
        this.userId = userId;
        this.role = role;
        this.expiresAt = expiresAt;
    }

    // Factory method: satu-satunya cara membuat session valid
    public static VerifiedAuthSession fromValidatedToken(String jwt) {
        var claims = JwtValidator.verify(jwt); // throws if invalid
        return new VerifiedAuthSession(
            claims.getSubject(),
            Role.fromServerToken(claims.get("role", String.class)),
            claims.getExpiration().toInstant()
        );
    }
    // NO PUBLIC CONSTRUCTOR — NO SETTERS
}
```

### Perbandingan Shallow vs Deep

| Aspek | Shallow Model | Deep Model |
|---|---|---|
| Auth state | `boolean isAuthenticated` (mutable) | `VerifiedAuthSession` (immutable, factory method) |
| Roles | `String[] roles` (klien bisa manipulasi) | `enum Role` (server-defined) |
| Credentials | Hardcoded plaintext di JS | Environment variables / Secret Vault |
| Login bypass | Override boolean → instant access | Server validates JWT → no bypass possible |

---

## 4. Kasus C — SEIMS (Deep Model)

### Domain Primitives

#### `SecureSession`
```java
public final class SecureSession {
    private final String userId;
    private final String token;
    private final String boundIpAddress;
    private final Instant expiresAt;

    public SecureSession(String userId, String token, String ipAddress, Instant expiresAt) {
        Objects.requireNonNull(userId, "userId wajib");
        Objects.requireNonNull(token, "token wajib");
        Objects.requireNonNull(ipAddress, "IP wajib");
        Objects.requireNonNull(expiresAt, "expiry wajib");

        if (!JwtValidator.isValidSignature(token)) throw new IllegalArgumentException("JWT invalid");
        if (!userId.equals(JwtValidator.extractUserId(token))) throw new IllegalArgumentException("Token not bound");
        if (expiresAt.isBefore(Instant.now())) throw new IllegalArgumentException("Expired");
        if (!IpAddress.isValid(ipAddress)) throw new IllegalArgumentException("Invalid IP");

        this.userId = userId;
        this.token = token;
        this.boundIpAddress = ipAddress;
        this.expiresAt = expiresAt;
    }
    // NO SETTERS — jika satu parameter tidak cocok, objek mustahil diciptakan
}
```

#### `IpAddress`
```java
public final class IpAddress {
    private static final Pattern IPV4 = Pattern.compile("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$");
    private final String value;

    public IpAddress(String raw) {
        if (raw == null || !IPV4.matcher(raw).matches()) {
            throw new IllegalArgumentException("Invalid IP address");
        }
        this.value = raw;
    }
}
```

### Perbandingan Shallow vs Deep

| Aspek | Shallow Model | Deep Model |
|---|---|---|
| Token validation | Menerima "Not Autorized" sebagai token valid | Validasi signature kriptografis |
| Identity binding | Token tanpa binding ke userId | `userId + token + IP` wajib cocok |
| Session expiry | Tidak ada expiry | `Instant expiresAt` dalam konstruktor |
| Employee IDs | Sequential BIGINT (enumerable) | UUID sebagai public identifier |
| API auth | Tidak ada Authorization header | Setiap request wajib `@PreAuthorize` |

---

## 5. Urutan Implementasi

```
Fase 1: ✅ Shallow Model Kasus A (selesai)
Fase 2: Shallow Model Kasus B + C
Fase 3: Deep Model Kasus A (VerifiedSession + EmployeeSearchQuery)
Fase 4: Deep Model Kasus B (Role enum + VerifiedAuthSession)
Fase 5: Deep Model Kasus C (SecureSession + IpAddress)
Fase 6: SecurityConfig update (protect /api/deep/**)
Fase 7: Unit tests comparing shallow vs deep behavior
```

---

## 6. Files to Create/Modify

### Fase 2 — Shallow Model B + C

| Action | File |
|---|---|
| CREATE | `shallow/InsecureProductSession.java` |
| CREATE | `shallow/InsecureProductHierarchyAPI.java` |
| CREATE | `shallow/ProductHierarchyShallowController.java` |
| CREATE | `shallow/InsecureSeimsSession.java` |
| CREATE | `shallow/InsecureSeimsAPI.java` |
| CREATE | `shallow/SeimsShallowController.java` |

### Fase 3-5 — Deep Models A + B + C

| Action | File |
|---|---|
| CREATE | `deep/domain/VerifiedSession.java` |
| CREATE | `deep/domain/EmployeeSearchQuery.java` |
| CREATE | `deep/domain/Role.java` |
| CREATE | `deep/domain/VerifiedAuthSession.java` |
| CREATE | `deep/domain/SecureSession.java` |
| CREATE | `deep/domain/IpAddress.java` |
| CREATE | `deep/SecureBusinessCardAPI.java` |
| CREATE | `deep/BusinessCardDeepController.java` |
| CREATE | `deep/SecureProductHierarchyAPI.java` |
| CREATE | `deep/ProductHierarchyDeepController.java` |
| CREATE | `deep/SecureSeimsAPI.java` |
| CREATE | `deep/SeimsDeepController.java` |

### Fase 6 — Security Config

| Action | File |
|---|---|
| MODIFY | `config/SecurityConfig.java` |
