package com.github.gelald.keycloak.dto;

import lombok.Data;

import java.util.Map;

/**
 * Keycloak health check response.
 */
@Data
public class HealthResponse {
    /** Overall health status: {@code "UP"} or {@code "DOWN"}. */
    private String status;
    /** Additional health check details as key-value pairs. */
    private Map<String, Object> details;
}
