# Secure Implementation Plan (Deep Logic Activation)

This document outlines the approach to fully migrating our unified application from "Shallow" functionality to the mature "Deep Model". Now that we have unified the endpoints into standard controller/service locations, we will activate the secure code by implementing strict Domain Primitives and replacing the insecure shallow paths.

## User Review Required

> [!WARNING]
> Implementing the Deep Models will introduce **BREAKING CHANGES** to the APIs.
> 1. Authentication will switch from raw tokens to strictly signed JWTs.
> 2. Passwords will be cryptographically checked (we will introduce a basic mock BCrypt hash check for demonstration strings if real Spring Security encoder is not fully mapped).
> 3. Unauthenticated access that previously worked because of "Shallow" bypasses will throw `400 Bad Request` or `401 Unauthorized` governed strictly by Domain Primitive constructors.
> Are you okay with breaking the existing insecure integrations entirely in favor of the Deep Model?

## Proposed Changes

We will work systematically through the phases defined in `deep-model-plan.md` to solidify the models.

### Phase 1: Realizing Cryptographic & Validation Primitives

#### [NEW] `util/JwtValidator.java`
- We will build a utility to mock or perform real Cryptographic JWT signature verification, extracting users, and checking expiry, which the Deep Models depend on.

#### [MODIFY] `domain/VerifiedSession.java` & `domain/EmployeeSearchQuery.java`
- Fully activate invariant checking. Throw exceptions on empty queries and bind JWTs strictly to `userId`.

#### [MODIFY] `domain/VerifiedAuthSession.java` & `domain/Role.java`
- Connect factory creation to JWT verification. Set strict bounds for ADMIN vs USER privilege logic.

#### [MODIFY] `domain/SecureSession.java` & `domain/IpAddress.java`
- Activate strict IPv4 regex validations and complex multi-factor bindings.

---

### Phase 2: Activating Deep Code in Controllers & Services

For all 3 domain clusters (**Business Card**, **Product Hierarchy**, and **SEIMS**):

#### [MODIFY] All Services (`BusinessCardService.java`, `ProductHierarchyService.java`, `SeimsService.java`)
- **Action**: Completely swap the active blocks. Comment out or delete the `=== KODE RENTAN (SHALLOW MODEL) ===` logic.
- **Action**: Uncomment, activate, and implement the `=== PERBAIKAN (DEEP MODEL) ===` logic. Implement necessary Role-Based Access logic (RBAC) bounded by the Domain Primitives.

#### [MODIFY] All Controllers (`BusinessCardController.java`, `ProductHierarchyController.java`, `SeimsController.java`)
- **Action**: Swap the active functionality. The endpoints will now intercept headers/parameters, construct the relevant Domain Primitive (which implies an implicit security barrier), and pass the verified primitive to the service layer.

---

### Phase 3: Documenting the Fixes

#### [MODIFY] `docs/API_Documentation.md`
- Update the payload parameters and expected strict Authorization headers required by the new Deep APIs.

## Verification Plan

### Automated Tests
- Build using `./mvnw clean compile` to catch structural issues.

### Manual Verification
- We will execute terminal `curl` requests against the endpoints during implementation.
- Observe that sending a blank `search` field to `/api/business-card/employees` now strictly returns a `400 Bad Request` instead of the whole database.
- Observe that missing or invalid JWTs correctly yield `401/403` HTTP codes governed by the primitives.
