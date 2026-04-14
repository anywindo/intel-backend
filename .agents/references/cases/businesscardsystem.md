# Kasus A: Business Card Portal — Analisis Sistem

## 1. System Overview

The Business Card System was an internal web application managed by Intel India Operations (IIO). The intended business purpose of this portal was to allow authorized Intel employees in India to search for their name in the corporate employee directory and subsequently format and order physical or electronic business cards based on that data. Despite its regional operational ownership, the portal's backend queried a global database, allowing users to search for employees worldwide.

## 2. System Structure & Data Flow

Based on the architectural footprint exposed during the assessment, the system relied on the following structure and data flow:

*   **Frontend Interface:** The client-side application was built as a single-page application (SPA) using the Angular framework.
*   **Authentication Integration:** The frontend utilized the Microsoft Authentication Library (MSAL) for JavaScript to verify user sessions and handle corporate Azure Single Sign-On (SSO) redirects.
*   **Backend APIs:**
    *   **Token API:** An endpoint (identified as `getAccessToken`) responsible for generating and returning authentication tokens to the client.
    *   **Worker API:** A backend data endpoint (e.g., `worker-snapshot-details`) that retrieved detailed employee records from the corporate database.
*   **Data Flow:** Under normal operation, the Angular frontend verifies the user's login state via MSAL, requests a token from the Token API, and then passes this token within an `Authorization` header to the Worker API. The frontend application appends a specific URL filter parameter (e.g., `?$filter=...`) to the Worker API request to constrain the database query to just the single employee being searched.

## 3. Rekonstruksi dalam Proyek Ini

Kasus A telah direkonstruksi sebagai **Shallow Model** di backend ini:

| Komponen Asli | Rekonstruksi di Proyek |
|---|---|
| MSAL JavaScript login bypass | `InsecureUserSession.java` — boolean `isAuthenticated` tanpa verifikasi server |
| Unauthenticated `getAccessToken` API | `InsecureBusinessCardAPI.generateAnonymousToken()` → hardcoded token |
| Worker API + URL filter manipulation | `InsecureBusinessCardAPI.getEmployeeData(token, searchFilter)` — `findAll()` jika filter kosong |
| Angular frontend | `ShallowController.java` — REST API `GET /api/shallow/token` dan `GET /api/shallow/employees` |
| 270.000 employee records | 151 dummy records di H2 (via `data.sql`) |

**Code Locations:**
- `src/main/java/org/example/securecoding/intelbackend/shallow/InsecureUserSession.java`
- `src/main/java/org/example/securecoding/intelbackend/shallow/InsecureBusinessCardAPI.java`
- `src/main/java/org/example/securecoding/intelbackend/shallow/ShallowController.java`

## 4. The Flaw (Vulnerability Analysis)

The system suffered from a catastrophic failure of defense-in-depth, relying heavily on a "Shallow Model" architecture where the backend blindly trusted client-side logic. This resulted in three distinct, overlapping vulnerabilities:

*   **Client-Side Trust (Broken Authentication):** The architecture violated the fundamental security principle of "Never Trust the Client" by delegating session verification entirely to the frontend JavaScript. The backend lacked independent validation of the user's authentication state, allowing the system to transition into an authenticated state simply by manipulating local variables in the browser.
*   **Insecure API (Broken Access Control):** The `getAccessToken` API was entirely unauthenticated. This Zero Trust violation allowed any anonymous, unauthenticated user to successfully request and receive a highly privileged JSON Web Token (JWT) directly from the server.
*   **Data Over-fetching & Client-Side Filtering (Primitive Obsession):** The Worker API failed to enforce the principle of least privilege or apply server-side data constraints. Instead of securely scoping the database query on the backend, the API relied on the frontend to pass a URL string filter to hide records. Because the search parameter lacked strict domain invariants (enforcing that a query cannot be empty), removing the filter caused the API to execute an unbounded query and dump the entire database to the client.

## 5. Exploit Mechanics

The vulnerability was manipulated through the following step-by-step execution:

1.  **Frontend Authentication Bypass:** The attacker intercepted the application's source code in the browser and modified the MSAL JavaScript file. By overriding the `getAllAccounts` function to return a non-empty array (using the payload `return;`), the attacker tricked the Angular application into believing a valid user was logged in, successfully bypassing the Azure SSO login screen.
2.  **Anonymous Token Generation:** Once past the login screen, the attacker observed the application's network traffic and sent a direct `GET` request to the unauthenticated `getAccessToken` API. The server responded with a valid, highly privileged Bearer token.
3.  **URL Filter Manipulation:** The attacker formulated a request to the `worker-snapshot-details` API using the stolen Bearer token. To bypass the intended application logic, the attacker deliberately stripped the `?$filter=` parameter from the API URL string.
4.  **Data Exfiltration:** Because the resulting data payload was so massive that it repeatedly crashed standard interception proxies like Fiddler and Postman, the attacker executed the manipulated request using the command-line tool `curl`.

```bash
curl --get --header "Authorization: Bearer eyJ0eXAiOiJKV1..." https://apis.intel.com/worker/v7/worker-snapshot-details?$format=json > intel-workers.json
```
*(Note: Payload truncated for brevity based on the provided source documentation)*

## 6. Security Impact

The blast radius of this specific architectural flaw was massive, entirely compromising the confidentiality of the corporate directory.

*   **Data Exposed:** The exploitation allowed the unauthenticated download of a nearly 1 GB JSON file containing the records of approximately 270,000 global Intel employees and workers.
*   **Compromised Fields:** The exfiltrated database fields included full names, corporate roles, manager hierarchies, phone numbers, and internal mailbox addresses.
*   **Excluded Data:** Highly sensitive regulatory and financial data, such as Social Security Numbers (SSNs) and salary information, were not exposed in this specific dataset.
*   **Secondary Risks:** While financial data was safe, the exposed organizational hierarchy and contact information provided threat actors with a comprehensive targeting map, feeding directly into potential identity theft, highly focused phishing schemes, and targeted social engineering attacks against the enterprise.

## 7. Deep Model Fix (Rencana Perbaikan)

| Kerentanan | Domain Primitive / Perbaikan |
|---|---|
| Boolean auth bypass | `VerifiedSession` — validasi token kriptografis di server, bukan boolean flag |
| Anonymous token API | Authenticated token endpoint — wajib menyertakan kredensial valid |
| Data over-fetching (Primitive Obsession) | `EmployeeSearchQuery` — konstruktor menolak null, empty, atau string terlalu pendek |
| IDOR / missing aggregate boundary | `AuthorizationBoundary` — `requesterId == targetId` atau `Role.ADMIN` |