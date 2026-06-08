package com.github.gelald.keycloak.client;

import com.github.gelald.keycloak.config.KeycloakOidcProperties;
import com.github.gelald.keycloak.dto.*;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Token 相关操作：获取、内省和构建授权 URL。
 *
 * @since 1.1
 */
class TokenOperations {

    private final KeycloakHttp http;
    private final KeycloakOidcProperties properties;

    TokenOperations(KeycloakHttp http, KeycloakOidcProperties properties) {
        this.http = http;
        this.properties = properties;
    }

    /**
     * 使用客户端凭据获取 token（机器对机器）。
     */
    TokenResponse getTokenByClientCredentials(ClientCredentialsTokenRequest request) {
        return http.postFormWithAuth(
                "/realms/{realm}/protocol/openid-connect/token",
                request.toMultiValueMap(),
                TokenResponse.class);
    }

    /**
     * 使用授权码换取 token（授权码 + PKCE 流程）。
     */
    TokenResponse getTokenByAuthCode(AuthCodeTokenRequest request) {
        return http.postFormWithAuth(
                "/realms/{realm}/protocol/openid-connect/token",
                request.toMultiValueMap(),
                TokenResponse.class);
    }

    /**
     * 使用 refresh token 刷新 access token。
     */
    TokenResponse getTokenByRefresh(RefreshTokenRequest request) {
        return http.postFormWithAuth(
                "/realms/{realm}/protocol/openid-connect/token",
                request.toMultiValueMap(),
                TokenResponse.class);
    }

    /**
     * 使用用户名密码获取 token（资源所有者密码凭据）。
     */
    TokenResponse getTokenByDirectFlow(DirectTokenRequest request) {
        return http.postFormWithAuth(
                "/realms/{realm}/protocol/openid-connect/token",
                request.toMultiValueMap(),
                TokenResponse.class);
    }

    /**
     * 内省 token，检查其是否有效。
     */
    IntrospectResponse introspect(IntrospectRequest request) {
        return http.postFormWithAuth(
                "/realms/{realm}/protocol/openid-connect/token/introspect",
                request.toMultiValueMap(),
                IntrospectResponse.class);
    }

    /**
     * 构建 Keycloak 授权 URL（浏览器重定向用）。
     */
    String authorizationUrl(String redirectUri, String state, String codeChallenge) {
        String authDomain = properties.getPublicDomain() != null ? properties.getPublicDomain() : properties.getDomain();
        return UriComponentsBuilder
                .fromHttpUrl(authDomain)
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
}
