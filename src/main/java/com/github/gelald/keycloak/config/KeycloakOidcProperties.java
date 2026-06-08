package com.github.gelald.keycloak.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Configuration properties for Keycloak OIDC client.
 *
*/
@Data
@ConfigurationProperties(prefix = "keycloak.oidc")
public class KeycloakOidcProperties {
    /**
     * Whether the Keycloak OIDC auto-configuration is enabled.
     * Works in conjunction with {@code @ConditionalOnProperty} on the auto-configuration class.
     */
    private boolean enabled = true;
    /**
     * Keycloak server internal domain URL.
     * Used for all server-to-server API calls (token introspection, token exchange, certs, etc.).
     * In container/K8s environments, this is typically the internal service address
     * (e.g. http://keycloak-service:8080).
     */
    private String domain;
    /**
     * Keycloak server public domain URL.
     * Used ONLY for constructing the browser redirect URL ({@code authorizationUrl}).
     * This must be externally accessible by the end-user's browser
     * (e.g. https://auth.example.com).
     * <p>
     * If not configured, falls back to {@link #domain}.
     */
    private String publicDomain;
    /**
     * Keycloak realm name
     */
    private String realm;
    /**
     * OAuth2 client ID
     */
    private String clientId;
    /**
     * OAuth2 client secret
     */
    private String clientSecret;
    /**
     * Connection timeout for HTTP requests to Keycloak server.
     */
    private Duration connectTimeout = Duration.ofSeconds(10);
    /**
     * Read timeout for HTTP requests to Keycloak server.
     */
    private Duration readTimeout = Duration.ofSeconds(30);
}
