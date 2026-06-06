package com.github.gelald.keycloak;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.gelald.keycloak.client.KeycloakOidcClient;
import com.github.gelald.keycloak.config.KeycloakOidcProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for Keycloak OIDC client.
 * <p>
 * Activates when {@code keycloak.oidc.enabled} is not set to {@code false}.
 *
*/
@AutoConfiguration
@ConditionalOnProperty(prefix = "keycloak.oidc", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(value = KeycloakOidcProperties.class)
public class KeycloakOidcAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public KeycloakOidcClient keycloakOidcClient(KeycloakOidcProperties properties,
                                                  ObjectMapper objectMapper) {
        return new KeycloakOidcClient(properties, objectMapper,
                properties.getConnectTimeout(), properties.getReadTimeout());
    }
}