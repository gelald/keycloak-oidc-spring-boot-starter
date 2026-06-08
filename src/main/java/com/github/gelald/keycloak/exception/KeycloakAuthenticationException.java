package com.github.gelald.keycloak.exception;

/**
 * Thrown when Keycloak returns 401 Unauthorized.
 *
*/
public class KeycloakAuthenticationException extends KeycloakOidcException {
    public KeycloakAuthenticationException(String error, String description) {
        super(401, error, description);
    }

    public KeycloakAuthenticationException(String error, String description, String rawBody) {
        super(401, error, description, rawBody);
    }
}
