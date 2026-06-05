package com.github.gelald.keycloak.dto;

import lombok.Data;

import java.util.Map;

/**
 * Keycloak health check response.
 *
*/
@Data
public class HealthResponse {
    /**
     * Overall status: "UP" or "DOWN".
     */
    private String status;
    /**
     * Additional health check details.
     */
    private Map<String, Object> details;
}
