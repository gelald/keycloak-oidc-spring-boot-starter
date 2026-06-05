package com.github.gelald.keycloak.exception;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Keycloak error response body.
 *
*/
@Data
public class KeycloakErrorResponse {
    @JsonProperty("error")
    private String error;
    @JsonProperty("error_description")
    private String errorDescription;
}
