package com.github.gelald.keycloak.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Keycloak OIDC client.
 *
*/
@Data
@ConfigurationProperties(prefix = "keycloak.oidc")
public class KeycloakOidcProperties {
    /**
     * Keycloak server domain URL (e.g. https://keycloak.example.com)
     */
    private String domain;
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
}
