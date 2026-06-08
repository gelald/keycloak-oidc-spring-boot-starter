package com.github.gelald.keycloak.client;

import com.github.gelald.keycloak.dto.CertificateResponse;
import com.github.gelald.keycloak.dto.HealthResponse;

/**
 * Provider 信息操作：获取证书和健康检查。
 * 这些端点不需要 Basic Auth 认证。
 *
 * @since 1.1
 */
class ProviderOperations {

    private final KeycloakHttp http;

    ProviderOperations(KeycloakHttp http) {
        this.http = http;
    }

    /**
     * 获取 JWKS 公钥，用于 JWT 签名验证。
     */
    CertificateResponse certs() {
        return http.getJson(
                "/realms/{realm}/protocol/openid-connect/certs",
                CertificateResponse.class);
    }

    /**
     * 检查 Keycloak 就绪状态（数据库等依赖项）。
     */
    HealthResponse ready() {
        return http.getJson("/health/ready", HealthResponse.class);
    }

    /**
     * 检查 Keycloak 存活状态。
     */
    HealthResponse live() {
        return http.getJson("/health/live", HealthResponse.class);
    }
}
