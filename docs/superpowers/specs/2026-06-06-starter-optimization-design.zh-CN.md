# Keycloak OIDC Spring Boot Starter - 社区 SDK 优化

**日期：** 2026-06-06
**状态：** 已批准
**范围：** v1.1.0 的增量改进，面向社区 SDK 采用

---

## 动机

作为社区 SDK，项目需要在三个方面进行改进：
1. **生产就绪** — 可配置超时、错误处理、HTTP 定制化
2. **开发者体验** — DTO 的 Builder 模式、更好的 Javadoc、国际化
3. **可扩展性** — 多 Realm 支持、内部重构以支持未来的 Reactive/Resilience4j

所有更改保持与 v1.0.0 的**向后兼容性**。

---

## 第一部分：属性增强 + HTTP 层定制化

### 1.1 添加缺失的属性

添加到 `KeycloakOidcProperties`：

```java
private boolean enabled = true;
private Duration connectTimeout = Duration.ofSeconds(10);
private Duration readTimeout = Duration.ofSeconds(30);
```

`enabled` 字段已被 `@ConditionalOnProperty` 隐式使用但未声明，导致 IDE 自动补全和配置元数据缺失。

超时设置替代了硬编码的 30 秒读取超时，并添加了之前缺失的连接超时。

### 1.2 RestClient.Builder 定制化点

修改 `KeycloakOidcAutoConfiguration` 以接受可选的 `RestClient.Builder`：

```java
@Bean
@ConditionalOnMissingBean
public KeycloakOidcClient keycloakOidcClient(
        KeycloakOidcProperties properties,
        ObjectMapper objectMapper,
        @Nullable RestClient.Builder restClientBuilder) {
    // 如果提供了自定义 builder 则使用，否则创建默认的
}
```

用户可以通过提供自己的 `RestClient.Builder` Bean 来定制 SSL、代理、拦截器。

### 1.3 将 Basic Auth 限定于需认证的端点

将 Basic Auth 从 `RestClient.builder().defaultHeader()` 移至各个 `postForm()`/`postFormVoid()` 调用中。公共端点（`certs()`、`ready()`、`live()`）不应发送凭据。

这可以防止在添加日志拦截器（v1.1 路线图）时发生凭据泄露。

---

## 第二部分：KeycloakOidcClient 内部重构

### 2.1 外观模式 + 内部辅助类

将 `KeycloakOidcClient` 重构为委托给包私有辅助类的外观模式：

```
KeycloakOidcClient（外观模式，公共 API 不变）
  ├── KeycloakHttp（包私有）
  │     RestClient 包装器、错误处理、Basic Auth
  ├── TokenOperations（包私有）
  │     getToken*、introspect、authorizationUrl
  ├── SessionOperations（包私有）
  │     logout、revoke
  └── ProviderOperations（包私有）
        certs、ready、live
```

公共方法签名保持不变。仅内部委托。

这为以下场景做准备：
- Reactive 支持（在现有类旁边添加 ReactiveTokenOperations）
- Resilience4j（在辅助类级别进行装饰）
- 未来的 Fluent API 迁移（v2.0）

### 2.2 错误处理增强

向 `KeycloakOidcException` 添加 `rawBody`：

```java
public class KeycloakOidcException extends RuntimeException {
    private final int status;
    private final String error;
    private final String description;
    private final String rawBody;  // 新增：原始 JSON 响应
}
```

子类 `KeycloakAuthenticationException` 和 `KeycloakAccessDeniedException` 继承此字段。

用户可以从 `rawBody` 中解析额外的 Keycloak 特定字段（例如 `error_uri`、自定义 SPI 错误数据）。

---

## 第三部分：DTO Builder 模式 + 多 Realm 支持

### 3.1 DTO 的 Builder 模式

为所有请求 DTO 添加 Lombok `@Builder`，同时保留现有的 setter：

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

两种使用方式均可：

```java
// 现有方式（仍然支持）
IntrospectRequest req = new IntrospectRequest();
req.setToken("abc");

// 新方式（推荐）
IntrospectRequest req = IntrospectRequest.builder()
    .token("abc")
    .tokenTypeHint("refresh_token")
    .build();
```

适用于：`AuthCodeTokenRequest`、`ClientCredentialsTokenRequest`、`DirectTokenRequest`、`RefreshTokenRequest`、`IntrospectRequest`、`LogoutRequest`、`RevokeRequest`。

### 3.2 通过工厂模式支持多 Realm

添加 `KeycloakOidcClientFactory` 作为 Spring 组件：

```java
@Component
public class KeycloakOidcClientFactory {
    private final KeycloakOidcProperties defaultProperties;
    private final ObjectMapper objectMapper;

    public KeycloakOidcClient create(String realm, String clientId, String clientSecret) { ... }
    public KeycloakOidcClient create(KeycloakOidcProperties properties) { ... }
}
```

自动配置注册两者：
- `KeycloakOidcClient` Bean（单 Realm、默认、向后兼容）
- `KeycloakOidcClientFactory` Bean（用于多 Realm 场景）

---

## 第四部分：文档和开发者体验

### 4.1 README 国际化

- 主 README：仅英文，移除代码示例中的中文注释
- 可选：`README.zh-CN.md` 供中文用户

### 4.2 CertificateData 可见性

将 `CertificateResponse.CertificateData` 从 `private static` 改为 `public static`。

### 4.3 DTO 字段的 Javadoc

为所有 DTO 字段添加 Javadoc，引用相关的 RFC/规范参数名。

### 4.4 版本策略

发布为 **v1.1.0**（所有更改均为向后兼容的新增内容）。

---

## 向后兼容性矩阵

| 变更 | 兼容 | 备注 |
|------|------|------|
| +enabled 属性 | 是 | 默认值为 true |
| +超时属性 | 是 | 与当前硬编码值相同的默认值 |
| +RestClient.Builder 注入 | 是 | 可选，`@Nullable` |
| Basic Auth 限定范围 | 行为变更 | 公共端点不再发送凭据（应无影响） |
| 内部外观模式重构 | 是 | 公共 API 不变 |
| +异常的 rawBody | 是 | 新增字段 |
| +DTO 的 Builder | 是 | 保留现有构造器/setter |
| +KeycloakOidcClientFactory | 是 | 新 Bean，不产生冲突 |
| CertificateData 可见性 | 是 | 扩大访问权限 |
| README 变更 | 不适用 | 仅文档 |

---

## 超出范围（未来版本）

- v1.2：请求/响应日志拦截器、DTO 输入验证
- v2.0：Fluent API 重新设计、Reactive WebClient 支持、Resilience4j 重试/熔断器
