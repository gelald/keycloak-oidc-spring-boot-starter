package com.github.gelald.keycloak.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * Translates Keycloak HTTP error status codes and response bodies
 * into typed {@link KeycloakOidcException} instances.
 *
*/
@Slf4j
public class KeycloakErrorDecoder {

    private final ObjectMapper objectMapper;

    public KeycloakErrorDecoder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Decode a Keycloak error response into the appropriate exception type.
     *
     * @param statusCode   HTTP status code
     * @param responseBody raw response body (JSON)
     * @return a typed exception (never null)
     */
    public RuntimeException decode(int statusCode, String responseBody) {
        KeycloakErrorResponse errorResponse = parseErrorResponse(responseBody);

        String error = errorResponse != null ? errorResponse.getError() : "unknown_error";
        String description = errorResponse != null ? errorResponse.getErrorDescription() : "Unknown error";

        log.warn("Keycloak OIDC error: status={}, error={}, description={}",
                statusCode, error, description);

        return switch (statusCode) {
            case 401 -> new KeycloakAuthenticationException(error, description);
            case 403 -> new KeycloakAccessDeniedException(error, description);
            default  -> new KeycloakOidcException(statusCode, error, description);
        };
    }

    private KeycloakErrorResponse parseErrorResponse(String body) {
        try {
            if (body != null && !body.isBlank()) {
                return objectMapper.readValue(body, KeycloakErrorResponse.class);
            }
        } catch (Exception e) {
            log.debug("Failed to parse Keycloak error response body", e);
        }
        return null;
    }
}