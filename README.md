# Keycloak OIDC Spring Boot Starter

A lightweight **Spring Boot Starter** for [Keycloak](https://www.keycloak.org/)'s **OpenID Connect / OAuth2 protocol endpoints**.

## Why This Library?

All existing Keycloak Java SDKs focus on the **Admin REST API** (user CRUD, realm management). This library fills a different gap — it wraps Keycloak's **OIDC protocol endpoints** for token lifecycle management.

| Feature | This Library | Official Admin Client |
|---------|-------------|----------------------|
| Token Issue (4 grant types) | Yes | No |
| Token Introspection (RFC 7662) | Yes | No |
| Token Revocation (RFC 7009) | Yes | No |
| Session Logout (OIDC) | Yes | No |
| JWKS Certificates | Yes | No |
| Health Checks | Yes | No |
| User / Realm CRUD | No | Yes |
| Dependency Footprint | Lightweight | Heavy (RestEasy) |

## Features

- **4 OAuth2 Grant Types**: Authorization Code (PKCE), Client Credentials, Password, Refresh Token
- **Token Introspection** (RFC 7662)
- **Token Revocation** (RFC 7009)
- **Session Logout** (OpenID Connect Session Management)
- **JWKS Public Key Retrieval** (RFC 7517)
- **Health Check Endpoints** (ready / live)
- **Spring Boot Auto-Configuration** — just add dependency and configure properties
- **Custom Exception Hierarchy** — structured error handling instead of raw HTTP status codes
- **PKCE (S256) Utilities** — `PkceUtils` for generating code_verifier and code_challenge

## Requirements

- Java 17+
- Spring Boot 3.2+
- Spring Web (RestClient) — no Feign / Spring Cloud dependency required
- A running Keycloak server

## Quick Start

### 1. Add Dependency

**Maven:**
```xml
<dependency>
    <groupId>com.github.gelald</groupId>
    <artifactId>keycloak-oidc-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Gradle:**
```groovy
implementation 'com.github.gelald:keycloak-oidc-spring-boot-starter:1.0.0'
```

### 2. Configure Properties

```yaml
keycloak:
  oidc:
    # Internal service URL for server-to-server API calls (token/cert/health etc.)
    domain: http://keycloak-service:8080
    # Public URL for browser redirects (authorizationUrl). Falls back to domain if not set.
    public-domain: https://auth.example.com
    realm: your-realm
    client-id: your-client-id
    client-secret: your-client-secret
```

### 3. Inject and Use

```java
@Autowired
private KeycloakOidcClient keycloakClient;

// Client Credentials Grant
ClientCredentialsTokenRequest request = new ClientCredentialsTokenRequest();
TokenResponse tokens = keycloakClient.getTokenByClientCredentials(request);
String accessToken = tokens.getAccessToken();

// Introspect Token
IntrospectRequest introspectRequest = new IntrospectRequest();
introspectRequest.setToken(accessToken);
IntrospectResponse introspectResponse = keycloakClient.introspect(introspectRequest);
boolean isActive = introspectResponse.getActive();

// Revoke Token
RevokeRequest revokeRequest = new RevokeRequest();
revokeRequest.setToken(accessToken);
keycloakClient.revoke(revokeRequest);
```

## Configuration Properties

| Property | Required | Default | Description |
|----------|----------|---------|-------------|
| `keycloak.oidc.domain` | Yes | — | Internal Keycloak service URL for server-to-server API calls |
| `keycloak.oidc.public-domain` | No | `domain` | Public Keycloak URL, used only for `authorizationUrl()` browser redirects |
| `keycloak.oidc.realm` | Yes | — | Realm name |
| `keycloak.oidc.client-id` | Yes | — | OAuth2 client ID |
| `keycloak.oidc.client-secret` | Yes | — | OAuth2 client secret |
| `keycloak.oidc.enabled` | No | `true` | Enable/disable auto-configuration |

## Usage Examples

### Authorization Code Flow with PKCE (Browser Flow)

The SDK provides PKCE utilities and authorization URL builder. Implement the redirect flow in your own controller:

```java
import com.github.gelald.keycloak.util.PkceUtils;

@Autowired
private KeycloakOidcClient client;

// Step 1: Redirect user to Keycloak login (uses public-domain to build URL)
@GetMapping("/login")
public void login(HttpSession session, HttpServletResponse resp) throws IOException {
    String verifier = PkceUtils.generateCodeVerifier();
    String challenge = PkceUtils.generateCodeChallengeS256(verifier);
    String state = UUID.randomUUID().toString();

    session.setAttribute("code_verifier", verifier);
    session.setAttribute("state", state);

    resp.sendRedirect(client.authorizationUrl(
            "https://your-app.com/callback", state, challenge));
}

// Step 2: Handle callback and exchange code for tokens
@GetMapping("/callback")
public String callback(@RequestParam String code,
                       @RequestParam String state,
                       HttpSession session) {
    if (!state.equals(session.getAttribute("state"))) {
        throw new SecurityException("Invalid state parameter");
    }
    String verifier = (String) session.getAttribute("code_verifier");

    AuthCodeTokenRequest request = new AuthCodeTokenRequest();
    request.setAuthCode(code);
    request.setRedirectUri("https://your-app.com/callback");
    request.setCodeVerifier(verifier);
    TokenResponse tokens = client.getTokenByAuthCode(request);

    return "Login success! access_token: " + tokens.getAccessToken();
}
```

### Refresh Token

```java
RefreshTokenRequest request = new RefreshTokenRequest();
request.setRefreshToken("your-refresh-token");
TokenResponse tokens = keycloakClient.getTokenByRefresh(request);
```

### Resource Owner Password Credentials

```java
DirectTokenRequest request = new DirectTokenRequest();
request.setUsername("user@example.com");
request.setPassword("password");
TokenResponse tokens = keycloakClient.getTokenByDirectFlow(request);
```

### Logout

```java
LogoutRequest request = new LogoutRequest();
request.setIdTokenHint("your-id-token");
keycloakClient.logout(request);
```

### Get JWKS Public Keys

```java
CertificateResponse certs = keycloakClient.certs();
// Use certs.getKeys() for JWT signature verification
```

### Health Checks

```java
HealthResponse ready = keycloakClient.ready();
HealthResponse live = keycloakClient.live();
```

### Custom Extension Parameters

```java
// For Keycloak SPI extensions or custom token exchange parameters
ClientCredentialsTokenRequest request = new ClientCredentialsTokenRequest();
Map<String, String> extParams = new HashMap<>();
extParams.put("custom-field", "custom-value");
request.setExtParams(extParams);
```

## Error Handling

The library provides a structured exception hierarchy:

```
KeycloakOidcException (base)
  ├── KeycloakAuthenticationException (401)
  └── KeycloakAccessDeniedException (403)
```

Each exception contains:
- `status` — HTTP status code
- `error` — Keycloak error code (e.g. `invalid_grant`)
- `description` — Human-readable error description

```java
try {
    keycloakClient.getTokenByClientCredentials(request);
} catch (KeycloakAuthenticationException e) {
    // Handle 401 - invalid client credentials
    log.error("Auth failed: {}", e.getDescription());
} catch (KeycloakOidcException e) {
    // Handle other Keycloak errors
    log.error("Keycloak error [{}]: {}", e.getError(), e.getDescription());
}
```

## Building from Source

```bash
mvn clean verify
```

## Roadmap

- [v1.1] Request/response logging interceptor
- [v1.1] Input validation on DTOs
- [v1.2] Retry / Circuit breaker (Resilience4j)
- [v1.2] Reactive WebClient support

## License

This project is licensed under the [Apache License 2.0](LICENSE).
