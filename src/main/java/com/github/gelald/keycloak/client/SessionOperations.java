package com.github.gelald.keycloak.client;

import com.github.gelald.keycloak.dto.LogoutRequest;
import com.github.gelald.keycloak.dto.RevokeRequest;

/**
 * Session 相关操作：登出和撤销 token。
 *
 * @since 1.1
 */
class SessionOperations {

    private final KeycloakHttp http;

    SessionOperations(KeycloakHttp http) {
        this.http = http;
    }

    /**
     * 使用 id_token_hint 结束会话。
     */
    void logout(LogoutRequest request) {
        http.postFormWithAuthVoid(
                "/realms/{realm}/protocol/openid-connect/logout",
                request.toMultiValueMap());
    }

    /**
     * 撤销 access token。
     */
    void revoke(RevokeRequest request) {
        http.postFormWithAuthVoid(
                "/realms/{realm}/protocol/openid-connect/revoke",
                request.toMultiValueMap());
    }
}
