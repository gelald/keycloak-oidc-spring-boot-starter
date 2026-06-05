package com.github.gelald.keycloak.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * OAuth2 grant types supported by the Keycloak OIDC client.
 *
* @see <a href="https://tools.ietf.org/html/rfc6749#section-1.3">RFC 6749 - Grant Types</a>
 */
@Getter
@AllArgsConstructor
public enum GrantTypeEnum {
    /**
     * Authorization Code Grant
     */
    AUTH_CODE("authorization_code"),
    /**
     * Client Credentials Grant
     */
    CLIENT("client_credentials"),
    /**
     * Resource Owner Password Credentials Grant
     */
    PASSWORD("password"),
    /**
     * Refresh Token Grant
     */
    REFRESH("refresh_token");

    private final String value;
}
