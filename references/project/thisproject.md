# Dokumen Instruksi Pembuatan Kode Rekonstruksi Keamanan (Proyek "Intel Outside")

## 1. Konteks Proyek
Proyek ini merupakan "Tugas Besar Pemrograman Aman A" (Tahap 1: Root Cause Reconstruction & Invariant Design) di bawah bimbingan dosen Wilifridus Bambang. Fokus utama tugas ini adalah menganalisis insiden keamanan siber "Intel Outside" dan merekonstruksi kelemahan desain (Root Cause) pada tiga sistem internal.

Tujuan akhir dari dokumen ini adalah untuk meminta LLM membuat contoh kode **Sebelum Perbaikan (Shallow Model)** dan **Sesudah Perbaikan (Deep Model/Secure by Design)** untuk tiga studi kasus web yang spesifik.

Aturan utama dalam perbaikan adalah menerapkan konsep **Domain-Driven Design (DDD) untuk Keamanan**, yaitu menggunakan *Domain Primitive*, *State Enum*, dan aturan *Invariant* di dalam konstruktor untuk memastikan *Invalid State Unrepresentable* (keadaan berbahaya mustahil direpresentasikan oleh kode).

---

## 2. Tech Stack & Arsitektur

| Komponen | Teknologi |
|---|---|
| Framework | Spring Boot 4.0.3 |
| Bahasa | Java 17 |
| Build Tool | Maven (via `mvnw` wrapper) |
| Database | H2 In-Memory (`jdbc:h2:mem:inteldb`) |
| ORM | Spring Data JPA + Hibernate |
| Security | Spring Security (dikonfigurasi *permit all* untuk Shallow Model) |
| Utility | Lombok (`@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`) |

**Root Package:** `org.example.securecoding.intelbackend`

**Package Structure saat ini:**
```
src/main/java/org/example/securecoding/intelbackend/
├── IntelBackendApplication.java              # Main entry point
├── config/
│   └── SecurityConfig.java                  # Spring Security config (permit all)
├── model/
│   ├── Employee.java                        # [Kasus A] JPA Entity
│   ├── Product.java                         # [Kasus B] Hierarki produk (self-ref)
│   ├── AdminCredential.java                 # [Kasus B] Plaintext credentials
│   ├── Supplier.java                        # [Kasus C] Sequential IDs
│   └── SupplierNda.java                     # [Kasus C] NDA dengan klasifikasi
├── repository/
│   ├── EmployeeRepository.java              # [Kasus A]
│   ├── ProductRepository.java               # [Kasus B]
│   ├── AdminCredentialRepository.java       # [Kasus B]
│   ├── SupplierRepository.java              # [Kasus C]
│   └── SupplierNdaRepository.java           # [Kasus C]
└── shallow/
    ├── InsecureUserSession.java              # [Kasus A] Boolean auth flag
    ├── InsecureBusinessCardAPI.java          # [Kasus A] Anon token + over-fetching
    ├── ShallowController.java               # [Kasus A] /api/shallow/*
    ├── InsecureProductSession.java           # [Kasus B] Boolean + String[] roles
    ├── InsecureProductHierarchyAPI.java      # [Kasus B] Plaintext auth + cred exposure
    ├── ProductHierarchyShallowController.java# [Kasus B] /api/shallow/product/*
    ├── InsecureSeimsSession.java             # [Kasus C] Broken JWT
    ├── InsecureSeimsAPI.java                 # [Kasus C] Sequential ID enum
    └── SeimsShallowController.java           # [Kasus C] /api/shallow/seims/*
```

---

## 3. Status Implementasi

| Kasus | Shallow Model | Deep Model |
|---|---|---|
| **A** — Business Card Portal | ✅ Selesai | ❌ Belum |
| **B** — Product Hierarchy | ✅ Selesai | ❌ Belum |
| **C** — SEIMS | ✅ Selesai | ❌ Belum |

---

## 4. Instruksi Umum untuk LLM (Code Generator)
Berdasarkan deskripsi setiap kasus di bawah, buatkan representasi kode berorientasi objek menggunakan **Java (Spring Boot)** yang memuat:
1. **Kode Vulnerable (Shallow Model):** Menunjukkan class yang rentan, ketiadaan validasi struktural, penggunaan tipe primitif (Primitive Obsession), dan bagaimana "state berbahaya" dapat dibuat.
2. **Simulasi Serangan:** Potongan kode singkat yang menunjukkan bagaimana *attacker* mengeksploitasi kelemahan desain tersebut.
3. **Kode Secure (Deep Model):** Menunjukkan perbaikan struktural menggunakan kelas *Domain Primitive*, *Value Object* yang *immutable*, dan penegakan *Invariant* secara ketat di dalam konstruktor.

---

## 5. Studi Kasus & Spesifikasi Pemodelan

### Kasus A: Sistem Kartu Nama (Business Card Portal) — ✅ Shallow Model Implemented
**Latar Belakang Insiden:** Penyerang berhasil mengunduh 270.000 data karyawan secara global tanpa otorisasi yang sah.
**Root Cause Desain:** Ketiadaan batasan agregat (*Aggregate Boundary*) dan kegagalan otorisasi objek langsung (IDOR). Sistem mengizinkan pembuatan permintaan data antar pengguna tanpa validasi kepemilikan.

**Implementasi Shallow Model saat ini:**
*   `InsecureUserSession.java` — Auth state adalah `boolean isAuthenticated` yang bisa di-set langsung via setter (tanpa verifikasi server-side).
*   `InsecureBusinessCardAPI.java` — `generateAnonymousToken()` mengembalikan token hardcoded tanpa autentikasi. `getEmployeeData()` menerima `String searchFilter` primitif tanpa invariant; jika kosong, memanggil `findAll()` (data breach).
*   `ShallowController.java` — REST endpoint `GET /api/shallow/token` dan `GET /api/shallow/employees`.

**Target Kode Deep Model (Perbaikan):**
*   Buat struktur di mana pengambilan data wajib menyertakan objek `SecureSession` atau `AuthorizationBoundary`.
*   `EmployeeSearchQuery` — Domain Primitive yang menolak null/empty/terlalu pendek di constructor.
*   `VerifiedSession` — Domain Primitive yang memvalidasi token secara kriptografis di sisi server.
*   Terapkan aturan ketat: pengguna hanya bisa menarik datanya sendiri (`requesterId == targetId`), kecuali jika objek sesi memiliki hak `Role.ADMIN`.

### Kasus B: Sistem Product Hierarchy
**Latar Belakang Insiden:** Situs web ini diretas melalui manipulasi kredensial administrator yang tertanam di kode (*hardcoded*) dan manipulasi peran (*bypass login*).
**Root Cause Desain:** *Shallow modeling* dan obsesi tipe primitif (*Primitive Obsession*). Validasi peran hanya menggunakan pencocokan teks (String) pada suatu array dasar yang dapat dimanipulasi dari input klien menjadi `isAuthenticated = true`.

**Target Kode Shallow Model:**
*   Menyimpan *password* di dalam variabel String.
*   Peran pengguna hanya dikelola dalam bentuk `String[] roles` (misalnya string "SPARK Product Management System Admin").
*   State berbahaya: Status login dapat disetel langsung melalui metode *setter* sederhana tanpa token yang divalidasi.

**Target Kode Deep Model (Perbaikan):**
*   Ganti String otorisasi dengan *Domain Primitive* khusus atau *State Enum* (misal: `Enum Role { USER, ADMIN }`).
*   Gunakan konstruktor otorisasi yang akan melempar *Exception* atau gagal diinisialisasi jika kredensial tidak tervalidasi secara kriptografis di sisi server.
*   Status `isAuthenticated` tidak boleh bisa disetel langsung (ketiadaan setter terbuka), melainkan harus melalui *transition method* yang tervalidasi.

### Kasus C: Sistem SEIMS (Supplier EHS IP Management System)
**Latar Belakang Insiden:** Sistem pertukaran IP pemasok diretas melalui dekripsi manipulasi token JSON Web Token (JWT).
**Root Cause Desain:** Manajemen sesi yang buruk (*Session Mismanagement*) dan tidak adanya aturan pengikatan identitas (*missing session binding*). Sistem mengizinkan inisiasi sesi tanpa mengikat token dengan pengguna asli.

**Target Kode Shallow Model:**
*   Aplikasi memiliki kelas sesi yang hanya berisi variabel string `token` tanpa ikatan ke `userId` yang jelas, atau token yang tidak memiliki parameter kedaluwarsa (*expiry*).
*   State berbahaya: Token sesi dapat dimanipulasi atau direpresentasikan dalam objek sesi meskipun token tidak valid atau milik orang lain.
*   Bisa ditunjukkan dengan implementasi "Billion Laughs" style di mana API menerima manipulasi input secara default tanpa pembatasan state ruang.

**Target Kode Deep Model (Perbaikan):**
*   Buat *Domain Primitive* untuk sesi, misalnya class `SecureSession`.
*   Terapkan *Invariant* di dalam konstruktor kelas tersebut: Sesi **wajib** memiliki pengikatan (*binding*) yang ketat antara `UserId`, `Token`, dan `IP Address`.
*   Jika salah satu parameter tidak cocok saat konstruksi atau pemanggilan state, objek sesi mustahil untuk diciptakan secara struktural (melempar *Exception*).

---

## 6. Panduan Ketat untuk Output LLM
1. Harap sertakan komentar (*comments*) pada bagian kode yang menjelaskan secara spesifik *bug* apa yang sedang dicegah.
2. Jangan menggunakan tipe data primitif secara bebas untuk merepresentasikan konsep domain. Bungkus tipe primitif menjadi kelas khusus (contoh: jangan gunakan `string email`, gunakan kelas `Email`).
3. Pastikan arsitektur memenuhi prinsip "Make Invalid State Unrepresentable".
4. Gunakan package structure yang konsisten: `org.example.securecoding.intelbackend.shallow.*` untuk Shallow Model dan `org.example.securecoding.intelbackend.deep.*` untuk Deep Model.