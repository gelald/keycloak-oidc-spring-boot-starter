package com.github.gelald.keycloak;

import com.github.gelald.keycloak.client.KeycloakOidcClient;
import com.github.gelald.keycloak.config.KeycloakOidcProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.time.Duration;

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

    @Test
    @DisplayName("Default timeout values are applied")
    void defaultTimeoutValues() {
        runner.run(context -> {
            KeycloakOidcProperties properties = context.getBean(KeycloakOidcProperties.class);
            assertThat(properties.isEnabled()).isTrue();
            assertThat(properties.getConnectTimeout()).isEqualTo(Duration.ofSeconds(10));
            assertThat(properties.getReadTimeout()).isEqualTo(Duration.ofSeconds(30));
        });
    }

    @Test
    @DisplayName("Custom timeout values are applied")
    void customTimeoutValues() {
        runner.withPropertyValues(
                        "keycloak.oidc.connect-timeout=5s",
                        "keycloak.oidc.read-timeout=60s")
                .run(context -> {
                    KeycloakOidcProperties properties = context.getBean(KeycloakOidcProperties.class);
                    assertThat(properties.getConnectTimeout()).isEqualTo(Duration.ofSeconds(5));
                    assertThat(properties.getReadTimeout()).isEqualTo(Duration.ofSeconds(60));
                });
    }

    @Test
    @DisplayName("自定义 RestClient.Builder 被注入到 KeycloakOidcClient")
    void customRestClientBuilderIsInjected() {
        runner.withUserConfiguration(CustomRestClientBuilderConfig.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(KeycloakOidcClient.class);
                    assertThat(context).hasBean("customRestClientBuilder");
                });
    }

    @Configuration
    static class CustomRestClientBuilderConfig {
        @Bean
        RestClient.Builder customRestClientBuilder() {
            return RestClient.builder()
                    .defaultHeader("X-Custom-Header", "from-custom-builder");
        }
    }
}
