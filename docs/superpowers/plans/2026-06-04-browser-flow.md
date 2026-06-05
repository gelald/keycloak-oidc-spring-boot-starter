# Browser Flow (Authorization Code + PKCE) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add PKCE utility methods and authorization URL builder to the SDK so users can implement Browser Flow (Authorization Code + PKCE) in their own controller.

**Architecture:** Two additions: (1) `PkceUtils` — static methods to generate `code_verifier` and `code_challenge` (S256); (2) `KeycloakOidcClient.authorizationUrl()` — builds the Keycloak authorize endpoint URL with PKCE params. The existing `getTokenByAuthCode()` + `AuthCodeTokenRequest` already handles the token exchange step.

**Tech Stack:** Java 17, Spring RestClient, JUnit 5 + AssertJ + WireMock

---

### Task 1: Add PkceUtils

**Files:**
- Create: `src/main/java/io/github/gelald/keycloak/util/PkceUtils.java`
- Create: `src/test/java/io/github/gelald/keycloak/util/PkceUtilsTest.java`

- [ ] **Step 1: Write failing test for PkceUtils**

```java
package com.github.gelald.keycloak.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;

import static org.assertj.core.api.Assertions.assertThat;

class PkceUtilsTest {

    @Test
    @DisplayName("generateCodeVerifier - returns a non-blank string")
    void generateCodeVerifier_nonBlank() {
        String verifier = PkceUtils.generateCodeVerifier();
        assertThat(verifier).isNotBlank();
    }

    @Test
    @DisplayName("generateCodeVerifier - returns URL-safe base64 string")
    void generateCodeVerifier_urlSafe() {
        String verifier = PkceUtils.generateCodeVerifier();
        assertThat(verifier).doesNotContain("+", "/", "=");
    }

    @Test
    @DisplayName("generateCodeVerifier - length is between 43 and 128 characters")
    void generateCodeVerifier_length() {
        String verifier = PkceUtils.generateCodeVerifier();
        assertThat(verifier).hasSizeBetween(43, 128);
    }

    @RepeatedTest(10)
    @DisplayName("generateCodeVerifier - produces different values each call")
    void generateCodeVerifier_randomness() {
        assertThat(PkceUtils.generateCodeVerifier())
                .isNotEqualTo(PkceUtils.generateCodeVerifier());
    }

    @Test
    @DisplayName("generateCodeChallengeS256 - produces consistent output for same input")
    void generateCodeChallengeS256_consistent() {
        String verifier = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk";
        String challenge1 = PkceUtils.generateCodeChallengeS256(verifier);
        String challenge2 = PkceUtils.generateCodeChallengeS256(verifier);
        assertThat(challenge1).isEqualTo(challenge2);
    }

    @Test
    @DisplayName("generateCodeChallengeS256 - output matches known RFC 7636 test vector")
    void generateCodeChallengeS256_knownVector() {
        String verifier = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk";
        // Expected: Base64URL-encoded SHA-256 of verifier
        String challenge = PkceUtils.generateCodeChallengeS256(verifier);
        assertThat(challenge).isEqualTo("E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM");
    }

    @Test
    @DisplayName("generateCodeChallengeS256 - produces URL-safe base64 string (no padding)")
    void generateCodeChallengeS256_urlSafe() {
        String challenge = PkceUtils.generateCodeChallengeS256(PkceUtils.generateCodeVerifier());
        assertThat(challenge).doesNotContain("+", "/", "=");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=PkceUtilsTest -Dsurefire.failIfNoSpecifiedTests=false`
Expected: COMPILATION ERROR (class not found)

- [ ] **Step 3: Write minimal implementation**

```java
package com.github.gelald.keycloak.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public final class PkceUtils {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int CODE_VERIFIER_BYTE_LENGTH = 32;

    private PkceUtils() {
    }

    public static String generateCodeVerifier() {
        byte[] bytes = new byte[CODE_VERIFIER_BYTE_LENGTH];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String generateCodeChallengeS256(String codeVerifier) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=PkceUtilsTest -Dsurefire.failIfNoSpecifiedTests=false`
Expected: BUILD SUCCESS, all 7 tests pass

---

### Task 2: Add authorizationUrl() to KeycloakOidcClient

**Files:**
- Modify: `src/main/java/io/github/gelald/keycloak/client/KeycloakOidcClient.java`
- Modify: `src/test/java/io/github/gelald/keycloak/client/KeycloakOidcClientTest.java`

- [ ] **Step 1: Write failing test**

Add to `KeycloakOidcClientTest.java`:

```java
@Test
@DisplayName("authorizationUrl - builds correct URL with PKCE params")
void authorizationUrl() {
    String url = client.authorizationUrl(
            "https://myapp.com/callback",
            "state-123",
            "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM");

    assertThat(url).startsWith(wireMock.baseUrl() + "/realms/test-realm/protocol/openid-connect/auth");
    assertThat(url).contains("response_type=code");
    assertThat(url).contains("client_id=test-client");
    assertThat(url).contains("redirect_uri=https%3A%2F%2Fmyapp.com%2Fcallback");
    assertThat(url).contains("state=state-123");
    assertThat(url).contains("code_challenge=E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM");
    assertThat(url).contains("code_challenge_method=S256");
    assertThat(url).contains("scope=openid");
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=KeycloakOidcClientTest#authorizationUrl -Dsurefire.failIfNoSpecifiedTests=false`
Expected: FAIL — compilation error, method not found

- [ ] **Step 3: Write minimal implementation**

Add to `KeycloakOidcClient.java`:

```java
public String authorizationUrl(String redirectUri, String state, String codeChallenge) {
    return UriComponentsBuilder
            .fromHttpUrl(properties.getDomain())
            .path("/realms/{realm}/protocol/openid-connect/auth")
            .queryParam("response_type", "code")
            .queryParam("client_id", properties.getClientId())
            .queryParam("redirect_uri", redirectUri)
            .queryParam("state", state)
            .queryParam("code_challenge", codeChallenge)
            .queryParam("code_challenge_method", "S256")
            .queryParam("scope", "openid")
            .build(properties.getRealm())
            .toString();
}
```

Add import at the top:
```java
import org.springframework.web.util.UriComponentsBuilder;
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=KeycloakOidcClientTest -Dsurefire.failIfNoSpecifiedTests=false`
Expected: BUILD SUCCESS, all tests pass

- [ ] **Step 5: Run full test suite to verify no regressions**

Run: `mvn test`
Expected: BUILD SUCCESS, all 48+ tests pass
