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
| Multi-Realm Factory | Yes | No |
| User / Realm CRUD | No | Yes |
| Dependency Footprint | Lightweight | Heavy (RestEasy) |

## Features

- **4 OAuth2 Grant Types**: Authorization Code (PKCE), Client Credentials, Password, Refresh Token
- **Token Introspection** (RFC 7662)
- **Token Revocation** (RFC 7009)
- **Session Logout** (OpenID Connect Session Management)
- **JWKS Public Key Retrieval** (RFC 7517)
- **Health Check Endpoints** (ready / live)
- **Multi-Realm Client Factory** — `KeycloakOidcClientFactory` for interacting with multiple Keycloak realms in a single application
- **Spring Boot Auto-Configuration** — just add dependency and configure properties
- **Custom Exception Hierarchy** — structured error handling instead of raw HTTP status codes
- **PKCE (S256) Utilities** — `PkceUtils` for generating code_verifier and code_challenge
- **Configurable Timeouts** — `connectTimeout` and `readTimeout` via properties

## Requirements

- Java 17+
- Spring Boot 3.3+
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
    # Optional: configure HTTP timeouts
    connect-timeout: 10s
    read-timeout: 30s
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

## Multi-Realm Support

The library provides a `KeycloakOidcClientFactory` for scenarios where a single application needs to interact with multiple Keycloak realms:

```java
@Autowired
private KeycloakOidcClientFactory clientFactory;

// Create a client for a specific realm — inherits domain, timeouts, etc.
KeycloakOidcClient realmClient = clientFactory.create("other-realm", "other-client-id", "other-secret");
TokenResponse tokens = realmClient.getTokenByClientCredentials(new ClientCredentialsTokenRequest());
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
| `keycloak.oidc.connect-timeout` | No | `10s` | Connection timeout for HTTP requests to Keycloak |
| `keycloak.oidc.read-timeout` | No | `30s` | Read timeout for HTTP requests to Keycloak |

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
// Optional: set scope and extension parameters
request.setScope("openid profile email");
Map<String, String> extParams = new HashMap<>();
extParams.put("custom-field", "custom-value");
request.setExtParams(extParams);
TokenResponse tokens = keycloakClient.getTokenByDirectFlow(request);
```

### Logout

```java
LogoutRequest request = new LogoutRequest();
request.setIdTokenHint("your-id-token");
// Optional: redirect after logout
request.setPostLogoutRedirectUri("https://your-app.com/home");
keycloakClient.logout(request);
```

### Get JWKS Public Keys

```java
CertificateResponse certs = keycloakClient.certs();
// Use certs.getKeys() for JWT signature verification
// Each CertificateData contains: kid, keyType, algorithm, use, modulus, exponent
```

### Health Checks

```java
HealthResponse ready = keycloakClient.ready();
HealthResponse live = keycloakClient.live();
// HealthResponse provides: status ("UP"/"DOWN"), details (Map<String, Object>)
```

## DTOs and Request/Response Objects

### Token Requests

| Class | Grant Type | Key Fields |
|-------|-----------|------------|
| `AuthCodeTokenRequest` | authorization_code | `authCode`, `redirectUri`, `codeVerifier`, `extParams` |
| `ClientCredentialsTokenRequest` | client_credentials | `scope`, `extParams` |
| `DirectTokenRequest` | password | `username`, `password`, `scope`, `extParams` |
| `RefreshTokenRequest` | refresh_token | `refreshToken`, `extParams` |

### Other Requests

| Class | Description | Key Fields |
|-------|-------------|------------|
| `IntrospectRequest` | Token introspection (RFC 7662) | `token`, `tokenTypeHint` |
| `RevokeRequest` | Token revocation (RFC 7009) | `token`, `tokenTypeHint` |
| `LogoutRequest` | OIDC session logout | `idTokenHint`, `postLogoutRedirectUri` |

### Response Objects

| Class | Description | Key Fields |
|-------|-------------|------------|
| `TokenResponse` | OAuth2/OIDC token response | `accessToken`, `refreshToken`, `idToken`, `expiresIn`, `tokenType`, `scope`, `sessionState`, `notBeforePolicy` |
| `IntrospectResponse` | Introspection result (RFC 7662) | `active`, `typ`, `exp`, `sub`, `username`, `aud`, `iss`, `iat`, `scope`, `clientId`, `realmAccess`, `resourceAccess` |
| `CertificateResponse` | JWKS key set (RFC 7517) | `keys` — list of `CertificateData` (kid, keyType, algorithm, use, modulus, exponent) |
| `HealthResponse` | Health check result | `status`, `details` |

### Grant Type Enum

The `GrantTypeEnum` covers all supported grant types:

| Enum Value | Grant Type Value |
|------------|-----------------|
| `AUTH_CODE` | `authorization_code` |
| `CLIENT` | `client_credentials` |
| `PASSWORD` | `password` |
| `REFRESH` | `refresh_token` |

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
- `rawBody` — Raw JSON response body from Keycloak

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

## License

This project is licensed under the [Apache License 2.0](LICENSE).