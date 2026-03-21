# Project Dependencies

**Build Tool:** Maven (via `mvnw` wrapper)
**Spring Boot Version:** 4.0.3
**Java Version:** 17

---

## Runtime Dependencies

| Dependency | Maven Artifact ID | Keterangan |
|---|---|---|
| Spring Web (MVC) | `spring-boot-starter-webmvc` | REST Controller, request handling |
| Spring Security | `spring-boot-starter-security` | Authentication & authorization framework |
| OAuth2 Resource Server | `spring-boot-starter-security-oauth2-resource-server` | JWT/OAuth2 token validation (untuk Deep Model) |
| Spring Data JPA | `spring-boot-starter-data-jpa` | ORM, Repository pattern |
| H2 Database | `h2` (runtime scope) | In-memory database untuk simulasi |
| H2 Console | `spring-boot-h2console` | Web UI untuk inspeksi database |
| Lombok | `lombok` (optional) | Boilerplate reduction (`@Data`, `@AllArgsConstructor`, dll.) |

## Test Dependencies

| Dependency | Maven Artifact ID | Scope |
|---|---|---|
| JPA Test | `spring-boot-starter-data-jpa-test` | test |
| Security Test | `spring-boot-starter-security-test` | test |
| OAuth2 RS Test | `spring-boot-starter-security-oauth2-resource-server-test` | test |
| Web MVC Test | `spring-boot-starter-webmvc-test` | test |

## Build Plugins

| Plugin | Keterangan |
|---|---|
| `maven-compiler-plugin` | Annotation processing untuk Lombok |
| `spring-boot-maven-plugin` | Packaging & running, excludes Lombok dari final JAR |