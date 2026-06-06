package com.github.gelald.keycloak.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.gelald.keycloak.config.KeycloakOidcProperties;
import com.github.gelald.keycloak.dto.*;
import org.springframework.web.client.RestClient;

/**
 * Keycloak OIDC 客户端，覆盖 OpenID Connect / OAuth2 协议端点。
 * 使用 Spring 的 {@link RestClient} 内部实现——无 Feign / Spring Cloud 依赖。
 * <p>
 * 此类为门面模式，内部委托给：
 * <ul>
 *   <li>{@link KeycloakHttp} - HTTP 层封装，处理 RestClient 配置、错误处理和认证作用域</li>
 *   <li>{@link TokenOperations} - Token 相关操作（获取、内省、授权 URL）</li>
 *   <li>{@link SessionOperations} - 会话相关操作（登出、撤销）</li>
 *   <li>{@link ProviderOperations} - Provider 信息操作（证书、健康检查）</li>
 * </ul>
 *
 * @see <a href="https://www.keycloak.org/docs/latest/securing_apps/">Keycloak Securing Applications</a>
 * @since 1.0
 */
public class KeycloakOidcClient {

    private final TokenOperations tokenOperations;
    private final SessionOperations sessionOperations;
    private final ProviderOperations providerOperations;

    /**
     * 使用自定义 RestClient.Builder 创建客户端。
     * <p>
     * 用户可以通过提供自定义的 RestClient.Builder 来配置 SSL、代理、拦截器等。
     *
     * @param properties        Keycloak OIDC 配置属性
     * @param objectMapper      JSON 序列化/反序列化器
     * @param restClientBuilder 自定义 RestClient.Builder，如果为 null 则使用默认构建器
     */
    public KeycloakOidcClient(KeycloakOidcProperties properties, ObjectMapper objectMapper,
                               RestClient.Builder restClientBuilder) {
        if (restClientBuilder == null) {
            restClientBuilder = RestClient.builder();
        }
        KeycloakHttp http = new KeycloakHttp(properties, objectMapper, restClientBuilder);
        this.tokenOperations = new TokenOperations(http, properties);
        this.sessionOperations = new SessionOperations(http);
        this.providerOperations = new ProviderOperations(http);
    }

    /**
     * 内省 token 以检查其是否有效。
     *
     * @see <a href="https://tools.ietf.org/html/rfc7662">RFC 7662 - OAuth 2.0 Token Introspection</a>
     */
    public IntrospectResponse introspect(IntrospectRequest introspectRequest) {
        return tokenOperations.introspect(introspectRequest);
    }

    /**
     * 使用授权码换取 token（授权码 + PKCE 流程）。
     *
     * @see <a href="https://tools.ietf.org/html/rfc6749#section-4.1">RFC 6749 - Authorization Code Grant</a>
     */
    public TokenResponse getTokenByAuthCode(AuthCodeTokenRequest authCodeTokenRequest) {
        return tokenOperations.getTokenByAuthCode(authCodeTokenRequest);
    }

    /**
     * 使用 refresh token 刷新过期的 access token。
     *
     * @see <a href="https://tools.ietf.org/html/rfc6749#section-6">RFC 6749 - Refreshing an Access Token</a>
     */
    public TokenResponse getTokenByRefresh(RefreshTokenRequest refreshTokenRequest) {
        return tokenOperations.getTokenByRefresh(refreshTokenRequest);
    }

    /**
     * 使用客户端凭据获取 token（机器对机器）。
     *
     * @see <a href="https://tools.ietf.org/html/rfc6749#section-4.4">RFC 6749 - Client Credentials Grant</a>
     */
    public TokenResponse getTokenByClientCredentials(ClientCredentialsTokenRequest clientCredentialsTokenRequest) {
        return tokenOperations.getTokenByClientCredentials(clientCredentialsTokenRequest);
    }

    /**
     * 使用用户名密码获取 token（资源所有者密码凭据）。
     *
     * @see <a href="https://tools.ietf.org/html/rfc6749#section-4.3">RFC 6749 - Resource Owner Password Credentials Grant</a>
     */
    public TokenResponse getTokenByDirectFlow(DirectTokenRequest directTokenRequest) {
        return tokenOperations.getTokenByDirectFlow(directTokenRequest);
    }

    /**
     * 使用 id_token_hint 结束会话。
     *
     * @see <a href="https://openid.net/specs/openid-connect-session-1_0.html">OpenID Connect Session Management</a>
     */
    public void logout(LogoutRequest logoutRequest) {
        sessionOperations.logout(logoutRequest);
    }

    /**
     * 撤销 access token。
     *
     * @see <a href="https://tools.ietf.org/html/rfc7009">RFC 7009 - OAuth 2.0 Token Revocation</a>
     */
    public void revoke(RevokeRequest revokeRequest) {
        sessionOperations.revoke(revokeRequest);
    }

    /**
     * 获取 JWKS 公钥，用于 JWT 签名验证。
     *
     * @see <a href="https://tools.ietf.org/html/rfc7517">RFC 7517 - JSON Web Key (JWK)</a>
     */
    public CertificateResponse certs() {
        return providerOperations.certs();
    }

    /**
     * 检查 Keycloak 就绪状态（数据库等依赖项）。
     */
    public HealthResponse ready() {
        return providerOperations.ready();
    }

    /**
     * 检查 Keycloak 存活状态。
     */
    public HealthResponse live() {
        return providerOperations.live();
    }

    /**
     * 构建 Keycloak 授权 URL（浏览器流程：授权码 + PKCE）。
     * <p>
     * 调用方应将最终用户浏览器重定向到此 URL。
     * 认证完成后，Keycloak 将重定向回来并带有授权码，
     * 可通过 {@link #getTokenByAuthCode(AuthCodeTokenRequest)} 换取。
     *
     * @param redirectUri   在 Keycloak 中注册的重定向 URI
     * @param state         CSRF / 会话状态值
     * @param codeChallenge PKCE code challenge（通过 {@link com.github.gelald.keycloak.util.PkceUtils#generateCodeChallengeS256(String)} 生成）
     * @return 完整的授权 URL
     */
    public String authorizationUrl(String redirectUri, String state, String codeChallenge) {
        return tokenOperations.authorizationUrl(redirectUri, state, codeChallenge);
    }
}
