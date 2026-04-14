# API Reference — Intel Backend

**Framework:** Spring Boot 4.0.3
**Base URL:** `http://localhost:8080`

---

## Shallow Model Endpoints

Package: `org.example.securecoding.intelbackend.shallow`
Controller: `ShallowController.java`
Service: `InsecureBusinessCardAPI.java`

### `GET /api/shallow/token`

Generates a privileged access token **without any authentication**. Simulates the unauthenticated `getAccessToken` API from the Intel breach.

**Request:**
```
GET /api/shallow/token
```

**Response:** `200 OK`
```json
{
  "accessToken": "SUPER_PRIVILEGED_TOKEN_123"
}
```

**Security Notes:**
- No credentials, session, or headers required
- Token value is hardcoded as `SUPER_PRIVILEGED_TOKEN_123` in `InsecureBusinessCardAPI.java`
- Demonstrates Zero Trust violation — anonymous users receive privileged tokens

---

### `GET /api/shallow/employees`

Searches employee records. Vulnerable to data over-fetching when the `search` parameter is empty.

**Request:**
```
GET /api/shallow/employees?token={accessToken}&search={query}
```

| Parameter | Type | Required | Description |
|---|---|---|---|
| `token` | String | ✅ Yes | Access token from `/api/shallow/token` |
| `search` | String | No (defaults to `""`) | Employee name filter (case-insensitive partial match) |

**Response (filtered — normal usage):** `200 OK`
```
GET /api/shallow/employees?token=SUPER_PRIVILEGED_TOKEN_123&search=Eaton
```
```json
{
  "count": 1,
  "data": [
    {
      "id": 1,
      "fullName": "Eaton Zveare",
      "role": "Security Researcher",
      "manager": "Jane Doe",
      "email": "eaton@intel.com",
      "phoneNumber": "+1-555-0000"
    }
  ]
}
```

**Response (empty search — THE VULNERABILITY):** `200 OK`
```
GET /api/shallow/employees?token=SUPER_PRIVILEGED_TOKEN_123&search=
```
```json
{
  "count": 151,
  "data": [
    { "id": 1, "fullName": "Eaton Zveare", ... },
    { "id": 2, "fullName": "Aditya Sharma", ... },
    "... 149 more records ..."
  ]
}
```

**Response (invalid token):** `401 Unauthorized`
```
GET /api/shallow/employees?token=WRONG&search=Eaton
```
```json
{
  "error": "Error: Invalid Token"
}
```

---

## Shallow Model Endpoints — Kasus B (Product Hierarchy)

Package: `org.example.securecoding.intelbackend.shallow`
Controller: `ProductHierarchyShallowController.java`
Service: `InsecureProductHierarchyAPI.java`

### `POST /api/shallow/product/login`

Authenticates using **plaintext password comparison** (`String.equals()`). No hashing, no salting.

**Request:**
```
POST /api/shallow/product/login?username=product-admin&password=IntelP%40ssw0rd2024%21
```

**Response (success):** `200 OK`
```json
{ "authenticated": true, "username": "product-admin", "role": "SPARK Product Management System Admin" }
```

**Response (wrong password):** `401 Unauthorized`
```json
{ "authenticated": false, "error": "Error: Invalid Credentials" }
```

### `GET /api/shallow/product/hierarchy`

Returns all 12 products with parent hierarchy. Only checks `session.isAuthenticated()` boolean — no server-side session validation.

**Response:** `200 OK` (after login)
```json
{ "count": 12, "data": [ { "productName": "Intel Processors", "parent": null }, ... ] }
```

### `GET /api/shallow/product/credentials`

**THE VULNERABILITY:** Exposes all admin credentials including plaintext passwords, GitHub PAT, and Basic Auth headers.

**Response:** `200 OK`
```json
{
  "count": 2,
  "data": [
    { "username": "product-admin", "password": "IntelP@ssw0rd2024!", "githubToken": "ghp_1a2b3c...", "basicAuth": "Basic cHJvZ..." }
  ]
}
```

---

## Shallow Model Endpoints — Kasus C (SEIMS)

Package: `org.example.securecoding.intelbackend.shallow`
Controller: `SeimsShallowController.java`
Service: `InsecureSeimsAPI.java`

### `POST /api/shallow/seims/auth`

Accepts **ANY non-empty Bearer token** string — including `"Not Autorized"` (the real breach string).

**Request:**
```
POST /api/shallow/seims/auth
Authorization: Bearer Not Autorized
```

**Response:** `200 OK`
```json
{ "authenticated": true, "token": "Not Autorized", "userId": "anonymous" }
```

### `GET /api/shallow/seims/suppliers`

Dumps all 10 suppliers. No authorization scoping.

**Response:** `200 OK`
```json
{ "count": 10, "data": [ { "id": 1, "companyName": "Taiwan Semiconductor Manufacturing", ... } ] }
```

### `GET /api/shallow/seims/suppliers/{id}`

**IDOR:** Sequential IDs allow trivial enumeration (`/suppliers/1`, `/suppliers/2`, ...).

**Response:** `200 OK`
```json
{ "data": { "id": 1, "companyName": "Taiwan Semiconductor Manufacturing", "email": "wchang@tsmc.com" } }
```

### `GET /api/shallow/seims/nda/{supplierId}`

Exposes confidential NDAs including **Top Secret** documents.

**Response:** `200 OK`
```json
{ "count": 2, "data": [ { "ndaTitle": "TSMC 3nm Process Node IP Agreement", "classification": "Top Secret" } ] }
```

---

## Deep Model Endpoints (Belum Diimplementasikan)

Endpoint berikut direncanakan untuk implementasi Deep Model (Secure by Design):

### Kasus A — Secure Business Card API
| Endpoint | Method | Keterangan |
|---|---|---|
| `/api/deep/businesscard/login` | `POST` | Autentikasi server-side, mengembalikan `VerifiedSession` |
| `/api/deep/businesscard/employees` | `GET` | Query yang di-scope oleh `EmployeeSearchQuery` domain primitive |

### Kasus B — Secure Product Hierarchy API
| Endpoint | Method | Keterangan |
|---|---|---|
| `/api/deep/product/login` | `POST` | Login dengan kredensial tervalidasi secara kriptografis |
| `/api/deep/product/hierarchy` | `GET` | Akses hierarki produk dengan role-based access control via `Role` enum |

### Kasus C — Secure SEIMS API
| Endpoint | Method | Keterangan |
|---|---|---|
| `/api/deep/seims/session` | `POST` | Pembuatan session dengan binding `UserId + Token + IP Address` |
| `/api/deep/seims/suppliers` | `GET` | Query data supplier dengan `SecureSession` domain primitive |

---

## H2 Database Console

### `GET /h2-console`

Web-based database management console for inspecting the in-memory H2 database directly.

| Field | Value |
|---|---|
| URL | `http://localhost:8080/h2-console` |
| JDBC URL | `jdbc:h2:mem:inteldb` |
| Username | `sa` |
| Password | `password` |

**Useful queries:**
```sql
-- Kasus A: Employee data
SELECT COUNT(*) FROM EMPLOYEE;                              -- 151 records
SELECT * FROM EMPLOYEE WHERE UPPER(FULL_NAME) LIKE '%EATON%';

-- Kasus B: Products & credentials
SELECT COUNT(*) FROM PRODUCT;                               -- 12 products
SELECT * FROM ADMIN_CREDENTIAL;                             -- Plaintext passwords!

-- Kasus C: Suppliers & NDAs
SELECT COUNT(*) FROM SUPPLIER;                              -- 10 suppliers
SELECT * FROM SUPPLIER_NDA WHERE classification = 'Top Secret';
SELECT * FROM USER_SESSION;                                 -- Broken tokens
```

---

## Data Models

### Employee (Kasus A)
| Field | Type | Description |
|---|---|---|
| `id` | Long | Auto-generated primary key |
| `fullName` | String | Employee's full name |
| `role` | String | Job title / engineering level |
| `manager` | String | Direct manager's name |
| `email` | String | Corporate email address |
| `phoneNumber` | String | Phone number with country code |

### Product (Kasus B)
| Field | Type | Description |
|---|---|---|
| `id` | Long | Auto-generated primary key |
| `productName` | String | Product name |
| `productCode` | String | Internal product code |
| `category` | String | e.g., Processors, Chipsets, NUC |
| `status` | String | e.g., Launched, Announced |
| `parent` | Product | Self-referencing @ManyToOne hierarchy |
| `createdBy` | String | Creator username |

### AdminCredential (Kasus B)
| Field | Type | Description |
|---|---|---|
| `username` | String | Admin username |
| `password` | String | **Plaintext!** Not hashed |
| `role` | String | Role as primitive String |
| `githubToken` | String | Exposed GitHub PAT |
| `basicAuth` | String | Plaintext Base64 Basic Auth header |

### Supplier (Kasus C)
| Field | Type | Description |
|---|---|---|
| `id` | Long | **Sequential** — enumerable! |
| `companyName` | String | Supplier company name |
| `contactPerson` | String | Point of contact |
| `email` | String | Contact email |
| `phone` | String | Phone number |
| `country` | String | Country |
| `ehsStatus` | String | e.g., Compliant, Pending, Non-Compliant |

### SupplierNda (Kasus C)
| Field | Type | Description |
|---|---|---|
| `supplier` | Supplier | @ManyToOne FK |
| `ndaTitle` | String | NDA document title |
| `signedDate` | LocalDate | Date signed |
| `expiryDate` | LocalDate | Expiry date |
| `classification` | String | Confidential or Top Secret |
| `documentUrl` | String | Link to NDA document |

---

## Security Configuration

File: `src/main/java/org/example/securecoding/intelbackend/config/SecurityConfig.java`

Konfigurasi saat ini (untuk Shallow Model) sengaja permit all — mensimulasikan ketiadaan backend access control:

```java
http
    .authorizeHttpRequests(auth -> auth
        .requestMatchers("/api/shallow/**").permitAll()
        .requestMatchers("/h2-console/**").permitAll()
        .anyRequest().permitAll())
    .csrf(csrf -> csrf.disable())
    .headers(headers -> headers
        .frameOptions(frame -> frame.disable()));
```

> **Untuk Deep Model:** SecurityConfig perlu diperketat — hanya `/api/shallow/**` yang tetap permit all, sedangkan `/api/deep/**` wajib melalui autentikasi berbasis JWT/OAuth2.

---

## CORS Configuration

By default, the backend allows requests from the same origin only. To connect from an Angular frontend on a different port, CORS must be configured in `SecurityConfig.java`:

```java
config.setAllowedOrigins(List.of("http://localhost:4200"));
```

---

## Running the Server

```bash
# Start the server
./mvnw spring-boot:run

# Compile only
./mvnw compile

# Run tests
./mvnw test
```

The server starts on port **8080** by default. The H2 database is seeded with **151 employee records**, **12 products**, **2 admin credentials**, **10 suppliers**, and **6 NDAs** automatically on every startup via `schema.sql` and `data.sql`.
