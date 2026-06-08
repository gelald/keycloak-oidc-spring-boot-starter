package com.github.gelald.keycloak.exception;

/**
 * Thrown when Keycloak returns 403 Forbidden.
 *
*/
public class KeycloakAccessDeniedException extends KeycloakOidcException {
    public KeycloakAccessDeniedException(String error, String description) {
        super(403, error, description);
    }

    public KeycloakAccessDeniedException(String error, String description, String rawBody) {
        super(403, error, description, rawBody);
    }
}
