# Keycloak OIDC Spring Boot Starter - Community SDK Optimization

**Date:** 2026-06-06
**Status:** Approved
**Scope:** Incremental improvements for v1.1.0, targeting community SDK adoption

---

## Motivation

As a community SDK, the project needs improvements in three areas:
1. **Production readiness** - configurable timeouts, error handling, HTTP customization
2. **Developer experience** - Builder pattern DTOs, better Javadoc, internationalization
3. **Extensibility** - multi-realm support, internal refactoring for future Reactive/Resilience4j

All changes maintain **backward compatibility** with v1.0.0.

---

## Part 1: Properties Enhancement + HTTP Layer Customization

### 1.1 Add Missing Properties

Add to `KeycloakOidcProperties`:

```java
private boolean enabled = true;
private Duration connectTimeout = Duration.ofSeconds(10);
private Duration readTimeout = Duration.ofSeconds(30);
```

The `enabled` field was implicitly used by `@ConditionalOnProperty` but not declared, causing missing IDE autocompletion and configuration metadata.

Timeouts replace the hardcoded 30-second read timeout and add previously missing connect timeout.

### 1.2 RestClient.Builder Customization Point

Modify `KeycloakOidcAutoConfiguration` to accept an optional `RestClient.Builder`:

```java
@Bean
@ConditionalOnMissingBean
public KeycloakOidcClient keycloakOidcClient(
        KeycloakOidcProperties properties,
        ObjectMapper objectMapper,
        @Nullable RestClient.Builder restClientBuilder) {
    // Use custom builder if provided, otherwise create default
}
```

Users can customize SSL, proxy, interceptors by providing their own `RestClient.Builder` bean.

### 1.3 Scope Basic Auth to Authenticated Endpoints Only

Move Basic Auth from `RestClient.builder().defaultHeader()` to individual `postForm()`/`postFormVoid()` calls. Public endpoints (`certs()`, `ready()`, `live()`) should not send credentials.

This prevents credential leakage when logging interceptors are added (v1.1 roadmap).

---

## Part 2: KeycloakOidcClient Internal Refactoring

### 2.1 Facade + Internal Helpers

Refactor `KeycloakOidcClient` into a facade delegating to package-private helpers:

```
KeycloakOidcClient (facade, public API unchanged)
  ├── KeycloakHttp (package-private)
  │     RestClient wrapper, error handling, Basic Auth
  ├── TokenOperations (package-private)
  │     getToken*, introspect, authorizationUrl
  ├── SessionOperations (package-private)
  │     logout, revoke
  └── ProviderOperations (package-private)
        certs, ready, live
```

Public method signatures remain identical. Internal delegation only.

This prepares for:
- Reactive support (add ReactiveTokenOperations alongside existing)
- Resilience4j (decorate at helper level)
- Future fluent API migration (v2.0)

### 2.2 Error Handling Enhancement

Add `rawBody` to `KeycloakOidcException`:

```java
public class KeycloakOidcException extends RuntimeException {
    private final int status;
    private final String error;
    private final String description;
    private final String rawBody;  // NEW: original JSON response
}
```

Subclasses `KeycloakAuthenticationException` and `KeycloakAccessDeniedException` inherit this field.

Users can parse additional Keycloak-specific fields from `rawBody` (e.g., `error_uri`, custom SPI error data).

---

## Part 3: DTO Builder Pattern + Multi-Realm Support

### 3.1 Builder Pattern for DTOs

Add Lombok `@Builder` to all request DTOs while keeping existing setters:

```java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IntrospectRequest {
    @Builder.Default
    private String tokenTypeHint = "access_token";
    private String token;
}
```

Both usage styles work:

```java
// Existing style (still supported)
IntrospectRequest req = new IntrospectRequest();
req.setToken("abc");

// New style (recommended)
IntrospectRequest req = IntrospectRequest.builder()
    .token("abc")
    .tokenTypeHint("refresh_token")
    .build();
```

Apply to: `AuthCodeTokenRequest`, `ClientCredentialsTokenRequest`, `DirectTokenRequest`, `RefreshTokenRequest`, `IntrospectRequest`, `LogoutRequest`, `RevokeRequest`.

### 3.2 Multi-Realm Support via Factory

Add `KeycloakOidcClientFactory` as a Spring component:

```java
@Component
public class KeycloakOidcClientFactory {
    private final KeycloakOidcProperties defaultProperties;
    private final ObjectMapper objectMapper;

    public KeycloakOidcClient create(String realm, String clientId, String clientSecret) { ... }
    public KeycloakOidcClient create(KeycloakOidcProperties properties) { ... }
}
```

Auto-configuration registers both:
- `KeycloakOidcClient` bean (single-realm, default, backward compatible)
- `KeycloakOidcClientFactory` bean (for multi-realm scenarios)

---

## Part 4: Documentation and Developer Experience

### 4.1 README Internationalization

- Main README: English only, remove Chinese comments from code examples
- Optional: `README.zh-CN.md` for Chinese-speaking users

### 4.2 CertificateData Visibility

Change `CertificateResponse.CertificateData` from `private static` to `public static`.

### 4.3 Javadoc for DTO Fields

Add Javadoc to all DTO fields referencing the relevant RFC/spec parameter name.

### 4.4 Version Strategy

Release as **v1.1.0** (all changes are backward-compatible additions).

---

## Backward Compatibility Matrix

| Change | Compatible | Notes |
|--------|-----------|-------|
| +enabled property | Yes | Defaults to true |
| +timeout properties | Yes | Same defaults as current hardcoded values |
| +RestClient.Builder injection | Yes | Optional, `@Nullable` |
| Basic Auth scoping | Behavioral change | Public endpoints no longer send credentials (should be harmless) |
| Internal facade refactoring | Yes | Public API unchanged |
| +rawBody on exceptions | Yes | Additive |
| +Builder on DTOs | Yes | Existing constructors/setters preserved |
| +KeycloakOidcClientFactory | Yes | New bean, doesn't conflict |
| CertificateData visibility | Yes | Widening access |
| README changes | N/A | Documentation only |

---

## Out of Scope (Future Versions)

- v1.2: Request/response logging interceptor, input validation on DTOs
- v2.0: Fluent API redesign, Reactive WebClient support, Resilience4j retry/circuit-breaker
