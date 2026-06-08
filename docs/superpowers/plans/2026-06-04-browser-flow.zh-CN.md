# 浏览器流程（授权码 + PKCE）实现计划

> **面向 Agent 工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐步实现本计划。步骤使用复选框（`- [ ]`）语法进行跟踪。

**目标：** 为 SDK 添加 PKCE 工具方法和授权 URL 构建器，使用户能够在自己的控制器中实现浏览器流程（授权码 + PKCE）。

**架构：** 两项新增内容：（1）`PkceUtils` — 静态方法，用于生成 `code_verifier` 和 `code_challenge`（S256）；（2）`KeycloakOidcClient.authorizationUrl()` — 构建包含 PKCE 参数的 Keycloak 授权端点 URL。现有的 `getTokenByAuthCode()` + `AuthCodeTokenRequest` 已处理令牌交换步骤。

**技术栈：** Java 17、Spring RestClient、JUnit 5 + AssertJ + WireMock

---

### 任务 1：添加 PkceUtils

**文件：**
- 新建：`src/main/java/io/github/gelald/keycloak/util/PkceUtils.java`
- 新建：`src/test/java/io/github/gelald/keycloak/util/PkceUtilsTest.java`

- [ ] **步骤 1：为 PkceUtils 编写失败测试**

```java
package com.github.gelald.keycloak.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;

import static org.assertj.core.api.Assertions.assertThat;

class PkceUtilsTest {

    @Test
    @DisplayName("generateCodeVerifier - 返回非空字符串")
    void generateCodeVerifier_nonBlank() {
        String verifier = PkceUtils.generateCodeVerifier();
        assertThat(verifier).isNotBlank();
    }

    @Test
    @DisplayName("generateCodeVerifier - 返回 URL 安全的 Base64 字符串")
    void generateCodeVerifier_urlSafe() {
        String verifier = PkceUtils.generateCodeVerifier();
        assertThat(verifier).doesNotContain("+", "/", "=");
    }

    @Test
    @DisplayName("generateCodeVerifier - 长度在 43 到 128 个字符之间")
    void generateCodeVerifier_length() {
        String verifier = PkceUtils.generateCodeVerifier();
        assertThat(verifier).hasSizeBetween(43, 128);
    }

    @RepeatedTest(10)
    @DisplayName("generateCodeVerifier - 每次调用产生不同的值")
    void generateCodeVerifier_randomness() {
        assertThat(PkceUtils.generateCodeVerifier())
                .isNotEqualTo(PkceUtils.generateCodeVerifier());
    }

    @Test
    @DisplayName("generateCodeChallengeS256 - 相同输入产生一致的输出")
    void generateCodeChallengeS256_consistent() {
        String verifier = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk";
        String challenge1 = PkceUtils.generateCodeChallengeS256(verifier);
        String challenge2 = PkceUtils.generateCodeChallengeS256(verifier);
        assertThat(challenge1).isEqualTo(challenge2);
    }

    @Test
    @DisplayName("generateCodeChallengeS256 - 输出匹配 RFC 7636 已知测试向量")
    void generateCodeChallengeS256_knownVector() {
        String verifier = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk";
        // 预期结果：verifier 的 Base64URL 编码 SHA-256 哈希值
        String challenge = PkceUtils.generateCodeChallengeS256(verifier);
        assertThat(challenge).isEqualTo("E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM");
    }

    @Test
    @DisplayName("generateCodeChallengeS256 - 生成 URL 安全的 Base64 字符串（无填充）")
    void generateCodeChallengeS256_urlSafe() {
        String challenge = PkceUtils.generateCodeChallengeS256(PkceUtils.generateCodeVerifier());
        assertThat(challenge).doesNotContain("+", "/", "=");
    }
}
```

- [ ] **步骤 2：运行测试验证其失败**

运行：`mvn test -Dtest=PkceUtilsTest -Dsurefire.failIfNoSpecifiedTests=false`
预期：编译错误（找不到类）

- [ ] **步骤 3：编写最小实现**

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

- [ ] **步骤 4：运行测试验证其通过**

运行：`mvn test -Dtest=PkceUtilsTest -Dsurefire.failIfNoSpecifiedTests=false`
预期：BUILD SUCCESS，全部 7 个测试通过

---

### 任务 2：为 KeycloakOidcClient 添加 authorizationUrl()

**文件：**
- 修改：`src/main/java/io/github/gelald/keycloak/client/KeycloakOidcClient.java`
- 修改：`src/test/java/io/github/gelald/keycloak/client/KeycloakOidcClientTest.java`

- [ ] **步骤 1：编写失败测试**

添加到 `KeycloakOidcClientTest.java`：

```java
@Test
@DisplayName("authorizationUrl - 构建包含 PKCE 参数的正确 URL")
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

- [ ] **步骤 2：运行测试验证其失败**

运行：`mvn test -Dtest=KeycloakOidcClientTest#authorizationUrl -Dsurefire.failIfNoSpecifiedTests=false`
预期：失败 — 编译错误，找不到方法

- [ ] **步骤 3：编写最小实现**

添加到 `KeycloakOidcClient.java`：

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

在顶部添加 import：
```java
import org.springframework.web.util.UriComponentsBuilder;
```

- [ ] **步骤 4：运行测试验证其通过**

运行：`mvn test -Dtest=KeycloakOidcClientTest -Dsurefire.failIfNoSpecifiedTests=false`
预期：BUILD SUCCESS，所有测试通过

- [ ] **步骤 5：运行完整测试套件验证无回归**

运行：`mvn test`
预期：BUILD SUCCESS，全部 48+ 个测试通过
