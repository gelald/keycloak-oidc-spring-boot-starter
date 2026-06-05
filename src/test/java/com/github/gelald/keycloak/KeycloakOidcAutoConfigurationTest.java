package com.github.gelald.keycloak;

import com.github.gelald.keycloak.client.KeycloakOidcClient;
import com.github.gelald.keycloak.config.KeycloakOidcProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Auto-configuration smoke tests.
 */
class KeycloakOidcAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    KeycloakOidcAutoConfiguration.class,
                    JacksonAutoConfiguration.class))
            .withPropertyValues(
                    "keycloak.oidc.domain=http://localhost:8080",
                    "keycloak.oidc.realm=test",
                    "keycloak.oidc.client-id=client",
                    "keycloak.oidc.client-secret=secret"
            );

    @Test
    @DisplayName("Auto-configuration activates with properties set")
    void autoConfigurationActivates() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(KeycloakOidcProperties.class);
            assertThat(context).hasSingleBean(KeycloakOidcClient.class);
        });
    }

    @Test
    @DisplayName("Auto-configuration does not activate when disabled")
    void autoConfigurationDisabled() {
        runner.withPropertyValues("keycloak.oidc.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(KeycloakOidcClient.class);
                });
    }
}