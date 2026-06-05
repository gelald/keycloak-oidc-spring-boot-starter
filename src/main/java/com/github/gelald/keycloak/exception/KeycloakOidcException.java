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

    public KeycloakOidcException(int status, String error, String description) {
        super(String.format("Keycloak OIDC error [%d %s]: %s", status, error, description));
        this.status = status;
        this.error = error;
        this.description = description;
    }

    public KeycloakOidcException(int status, String error, String description, Throwable cause) {
        super(String.format("Keycloak OIDC error [%d %s]: %s", status, error, description), cause);
        this.status = status;
        this.error = error;
        this.description = description;
    }
}
