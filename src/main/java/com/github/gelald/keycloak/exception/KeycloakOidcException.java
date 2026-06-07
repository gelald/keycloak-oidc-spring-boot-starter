package com.github.gelald.keycloak.exception;

import lombok.Getter;

/**
 * Base exception for all Keycloak OIDC client errors.
 *
*/
@Getter
public class KeycloakOidcException extends RuntimeException {
    private final int status;
    private final String error;
    private final String description;
    /** 原始 JSON 响应体。通过遗留构造器创建时可能为 {@code null}。 */
    private final String rawBody;

    public KeycloakOidcException(int status, String error, String description) {
        this(status, error, description, null, null);
    }

    public KeycloakOidcException(int status, String error, String description, String rawBody) {
        this(status, error, description, rawBody, null);
    }

    public KeycloakOidcException(int status, String error, String description, Throwable cause) {
        this(status, error, description, null, cause);
    }

    public KeycloakOidcException(int status, String error, String description, String rawBody, Throwable cause) {
        super(String.format("Keycloak OIDC error [%d %s]: %s", status, error, description), cause);
        this.status = status;
        this.error = error;
        this.description = description;
        this.rawBody = rawBody;
    }
}
