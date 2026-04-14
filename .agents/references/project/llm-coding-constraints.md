# LLM Coding Constraints & Code Quality Standards

Dokumen ini mendefinisikan aturan ketat yang **wajib** diikuti oleh LLM saat menghasilkan kode untuk proyek ini. Semua constraint diturunkan dari pola yang sudah ada di codebase.

---

## 1. Project Constraints

### 1.1 Framework & Language
| Rule | Value |
|---|---|
| Framework | Spring Boot 4.0.3 — **jangan** gunakan versi lain |
| Java Version | Java 17 — **jangan** gunakan fitur Java 21+ (e.g., record patterns, string templates) |
| Build Tool | Maven via `mvnw` — **jangan** gunakan Gradle |
| Database | H2 In-Memory — **jangan** gunakan MySQL/PostgreSQL untuk proyek ini |
| ORM | Spring Data JPA — **jangan** gunakan MyBatis atau JDBC langsung kecuali di `schema.sql`/`data.sql` |

### 1.2 Package Naming
```
org.example.securecoding.intelbackend          # Root package
org.example.securecoding.intelbackend.config    # Configuration classes
org.example.securecoding.intelbackend.model     # JPA Entities
org.example.securecoding.intelbackend.repository # JPA Repositories
org.example.securecoding.intelbackend.shallow   # Shallow Model (vulnerable code)
org.example.securecoding.intelbackend.deep      # Deep Model (secure code)
org.example.securecoding.intelbackend.deep.domain # Domain Primitives
```

> **DILARANG:** Menggunakan package name lain seperti `id.ac.uajy.informatika.*` atau `com.intel.*`.

### 1.3 Dependency Rules
- **HANYA** gunakan dependency yang sudah ada di `pom.xml`
- Jika butuh dependency baru, **dokumentasikan** alasannya dan minta persetujuan
- **JANGAN** menambahkan dependency hanya untuk convenience (e.g., Guava untuk `Preconditions` — gunakan `Objects.requireNonNull` bawaan Java)

---

## 2. Code Quality Standards

### 2.1 Class Design — Wajib Mengikuti Pola yang Ada

**Entity classes** harus menggunakan Lombok seperti `Employee.java`:
```java
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // fields...
}
```

**Service classes** menggunakan constructor injection (bukan `@Autowired`):
```java
// ✅ BENAR — seperti InsecureBusinessCardAPI.java
@Service
public class InsecureBusinessCardAPI {
    private final EmployeeRepository employeeRepository;

    public InsecureBusinessCardAPI(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }
}

// ❌ SALAH — jangan gunakan field injection
@Service
public class InsecureBusinessCardAPI {
    @Autowired
    private EmployeeRepository employeeRepository;
}
```

**Controller classes** menggunakan constructor injection:
```java
// ✅ BENAR — seperti ShallowController.java
@RestController
@RequestMapping("/api/shallow")
public class ShallowController {
    private final InsecureBusinessCardAPI insecureBusinessCardAPI;

    public ShallowController(InsecureBusinessCardAPI insecureBusinessCardAPI) {
        this.insecureBusinessCardAPI = insecureBusinessCardAPI;
    }
}
```

### 2.2 API Response Format

Selalu gunakan `ResponseEntity<?>` dengan `Map.of()` untuk response body:
```java
// ✅ BENAR — format yang konsisten
return ResponseEntity.ok(Map.of(
    "count", employees.size(),
    "data", employees
));

// ✅ BENAR — error response
return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));

// ❌ SALAH — jangan return entity langsung
return ResponseEntity.ok(employees);

// ❌ SALAH — jangan buat DTO class terpisah untuk response sederhana
return ResponseEntity.ok(new EmployeeResponse(employees));
```

### 2.3 Error Handling

Gunakan `SecurityException` untuk error autentikasi/otorisasi, ditangkap di controller:
```java
// Service layer — throw exception
if (!PRIVILEGED_TOKEN.equals(token)) {
    throw new SecurityException("Error: Invalid Token");
}

// Controller layer — catch dan convert jadi HTTP response
try {
    List<Employee> employees = api.getEmployeeData(token, search);
    return ResponseEntity.ok(Map.of("count", employees.size(), "data", employees));
} catch (SecurityException e) {
    return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
}
```

---

## 3. Shallow Model Constraints

### 3.1 Intentional Vulnerabilities — WAJIB Didokumentasikan

Setiap kerentanan yang sengaja dibuat **WAJIB** memiliki komentar Javadoc yang menjelaskan:
1. Nomor vulnerability (e.g., `VULNERABILITY 1`)
2. Apa yang disimulasikan dari breach asli
3. Mengapa ini rentan

```java
/**
 * SHALLOW MODEL — VULNERABILITY 1: Anemic Domain Model & Boolean Flag Hell
 *
 * Authentication state is represented by a plain boolean that can be freely
 * manipulated without any backend verification. This mirrors the Intel breach
 * where the attacker modified the MSAL JavaScript 'getAllAccounts' function.
 *
 * THE FLAW: There is no server-side session validation.
 */
```

### 3.2 Naming Convention untuk Shallow Model

| Tipe | Prefix | Contoh |
|---|---|---|
| Session class | `Insecure` | `InsecureUserSession`, `InsecureProductSession` |
| API/Service class | `Insecure` | `InsecureBusinessCardAPI`, `InsecureSeimsAPI` |
| Controller class | — (gunakan nama deskriptif) | `ShallowController`, `SeimsShallowController` |

### 3.3 Shallow Model — Yang Diperbolehkan
- ✅ Hardcoded tokens/credentials (ini vulnerability yang disengaja)
- ✅ `findAll()` tanpa limit (simulasi data over-fetching)
- ✅ Boolean flags untuk auth state (simulasi client-side trust)
- ✅ `String` untuk roles (simulasi primitive obsession)

### 3.4 Shallow Model — Yang DILARANG
- ❌ Bug yang tidak disengaja atau tidak terdokumentasikan
- ❌ Code yang tidak bisa di-compile
- ❌ SQL injection atau kerentanan yang bukan bagian dari studi kasus
- ❌ Merusak fungsionalitas shallow model yang sudah ada

---

## 4. Deep Model Constraints

### 4.1 Domain Primitive Rules

Setiap Domain Primitive **WAJIB**:
1. Dideklarasikan `final` (tidak bisa di-extend)
2. Tidak memiliki setter publik (immutable)
3. Memvalidasi semua invariant di konstruktor
4. Melempar `IllegalArgumentException` jika invariant dilanggar
5. Memiliki komentar yang menjelaskan invariant apa yang ditegakkan

```java
// ✅ BENAR — Domain Primitive pattern
public final class EmployeeSearchQuery {
    private final String value;

    public EmployeeSearchQuery(String raw) {
        // INVARIANT 1: Non-null
        Objects.requireNonNull(raw, "Search query tidak boleh null");
        // INVARIANT 2: Non-empty
        if (raw.trim().isEmpty()) {
            throw new IllegalArgumentException("Search query tidak boleh kosong");
        }
        // INVARIANT 3: Minimum length
        if (raw.trim().length() < 2) {
            throw new IllegalArgumentException("Search query minimal 2 karakter");
        }
        this.value = raw.trim();
    }

    public String getValue() { return value; }
    // NO SETTERS
}
```

### 4.2 Naming Convention untuk Deep Model

| Tipe | Prefix/Pattern | Contoh |
|---|---|---|
| Session class | `Verified` / `Secure` | `VerifiedSession`, `SecureSession` |
| API/Service class | `Secure` | `SecureBusinessCardAPI` |
| Controller class | — (deskriptif) | `BusinessCardDeepController` |
| Domain Primitive | Nama domain concept | `EmployeeSearchQuery`, `Role`, `IpAddress` |

### 4.3 Deep Model — Yang WAJIB
- ✅ Semua tipe primitif domain dibungkus dalam class khusus
- ✅ Constructor validation untuk setiap Domain Primitive
- ✅ `Objects.requireNonNull()` untuk semua parameter
- ✅ Komentar menjelaskan bug/invariant apa yang dicegah
- ✅ `final` class dan `final` fields

### 4.4 Deep Model — Yang DILARANG
- ❌ Public setter pada Domain Primitive
- ❌ `String` untuk representasi role/permission (gunakan `enum`)
- ❌ Boolean flag untuk authentication state
- ❌ Hardcoded credentials
- ❌ `findAll()` tanpa scoping

---

## 5. File & Formatting Standards

### 5.1 File Structure
Setiap Java file harus mengikuti urutan:
1. `package` declaration
2. `import` statements (dikelompokkan: Java stdlib, Jakarta, Spring, project)
3. Class Javadoc
4. Class declaration
5. Constants (`private static final`)
6. Instance fields
7. Constructor(s)
8. Public methods
9. Private methods

### 5.2 Javadoc Requirements
- **Wajib** untuk setiap class (menjelaskan tujuan dan kasus mana yang direpresentasikan)
- **Wajib** untuk setiap public method di service/API classes
- **Direkomendasikan** untuk constructor di Domain Primitives (sebutkan invariant)

### 5.3 Formatting
- Indentasi: **4 spasi** (bukan tab)
- Brace style: K&R (opening brace di baris yang sama)
- Max line length: 120 karakter
- Blank line antara methods

---

## 6. Database Rules

### 6.1 Schema Changes
- Definisikan schema di `schema.sql` — **jangan** gunakan `ddl-auto=update`
- Gunakan `CREATE TABLE IF NOT EXISTS` untuk idempotency
- Column names menggunakan `snake_case` (e.g., `full_name`, `phone_number`)

### 6.2 Seed Data
- Tambahkan data di `data.sql` — **jangan** gunakan `CommandLineRunner`/`@PostConstruct`
- Record pertama selalu `Eaton Zveare` (target dari breach asli)
- Gunakan nama realistis dari berbagai negara untuk diversity

### 6.3 Repository
- Extend `JpaRepository<Entity, Long>`
- Gunakan Spring Data derived query methods (e.g., `findByFullNameContainingIgnoreCase`)
- **Jangan** gunakan `@Query` dengan native SQL kecuali benar-benar diperlukan

---

## 7. Testing Standards

### 7.1 Test Naming
```java
@Test
void shouldReturnAllEmployees_whenSearchFilterIsEmpty() { }

@Test
void shouldThrowException_whenTokenIsInvalid() { }

@Test
void shouldRejectConstruction_whenQueryIsNull() { }  // Domain Primitive test
```

### 7.2 Test Coverage Priorities
1. **Domain Primitives** — test semua invariant (happy path + setiap rejection case)
2. **Service layer** — test vulnerability behavior (shallow) dan prevention (deep)
3. **Controller layer** — test HTTP status codes dan response format
