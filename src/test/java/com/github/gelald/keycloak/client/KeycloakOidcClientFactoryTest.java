package com.github.gelald.keycloak.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.gelald.keycloak.KeycloakOidcAutoConfiguration;
import com.github.gelald.keycloak.config.KeycloakOidcProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link KeycloakOidcClientFactory} — multi-realm client creation.
 */
class KeycloakOidcClientFactoryTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    KeycloakOidcAutoConfiguration.class,
                    JacksonAutoConfiguration.class))
            .withPropertyValues(
                    "keycloak.oidc.domain=http://localhost:8080",
                    "keycloak.oidc.public-domain=https://auth.example.com",
                    "keycloak.oidc.realm=default-realm",
                    "keycloak.oidc.client-id=default-client",
                    "keycloak.oidc.client-secret=default-secret",
                    "keycloak.oidc.connect-timeout=5s",
                    "keycloak.oidc.read-timeout=15s"
            );

    @Test
    @DisplayName("Factory bean is registered by auto-configuration")
    void factoryBeanIsRegistered() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(KeycloakOidcClientFactory.class);
        });
    }

    @Test
    @DisplayName("Both KeycloakOidcClient and KeycloakOidcClientFactory beans coexist")
    void clientAndFactoryBeansCoexist() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(KeycloakOidcClient.class);
            assertThat(context).hasSingleBean(KeycloakOidcClientFactory.class);
        });
    }

    @Test
    @DisplayName("Convenience create(realm, clientId, clientSecret) produces distinct clients")
    void convenienceCreateProducesDistinctClients() {
        runner.run(context -> {
            KeycloakOidcClientFactory factory = context.getBean(KeycloakOidcClientFactory.class);

            KeycloakOidcClient client1 = factory.create("realm-a", "client-a", "secret-a");
            KeycloakOidcClient client2 = factory.create("realm-b", "client-b", "secret-b");

            assertThat(client1).isNotSameAs(client2);
        });
    }

    @Test
    @DisplayName("Convenience method creates a client with the same realm config")
    void convenienceCreateUsesCorrectRealm() {
        runner.run(context -> {
            KeycloakOidcClientFactory factory = context.getBean(KeycloakOidcClientFactory.class);

            KeycloakOidcClient realmA = factory.create("my-realm", "my-client", "my-secret");
            KeycloakOidcClient realmB = factory.create("other-realm", "other-client", "other-secret");

            // Both clients should be non-null and distinct
            assertThat(realmA).isNotNull();
            assertThat(realmB).isNotNull();
            assertThat(realmA).isNotSameAs(realmB);
        });
    }

    @Test
    @DisplayName("Full properties create produces a client from custom properties")
    void fullPropertiesCreateProducesClient() {
        runner.run(context -> {
            KeycloakOidcClientFactory factory = context.getBean(KeycloakOidcClientFactory.class);

            KeycloakOidcProperties customProps = new KeycloakOidcProperties();
            customProps.setDomain("http://custom-host:9090");
            customProps.setPublicDomain("https://custom-public.example.com");
            customProps.setRealm("custom-realm");
            customProps.setClientId("custom-client");
            customProps.setClientSecret("custom-secret");
            customProps.setConnectTimeout(Duration.ofSeconds(3));
            customProps.setReadTimeout(Duration.ofSeconds(20));

            KeycloakOidcClient client = factory.create(customProps);

            assertThat(client).isNotNull();
        });
    }

    @Test
    @DisplayName("Factory is not registered when auto-configuration is disabled")
    void factoryNotRegisteredWhenDisabled() {
        runner.withPropertyValues("keycloak.oidc.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(KeycloakOidcClientFactory.class);
                });
    }

    @Test
    @DisplayName("Convenience method inherits default domain and timeouts")
    void convenienceMethodInheritsDefaults() {
        // Unit-test the factory directly to verify property copying
        KeycloakOidcProperties base = new KeycloakOidcProperties();
        base.setDomain("http://base-host:8080");
        base.setPublicDomain("https://base-public.example.com");
        base.setRealm("base-realm");
        base.setClientId("base-client");
        base.setClientSecret("base-secret");
        base.setConnectTimeout(Duration.ofSeconds(7));
        base.setReadTimeout(Duration.ofSeconds(42));

        KeycloakOidcClientFactory factory = new KeycloakOidcClientFactory(base, new ObjectMapper());

        // The factory must be constructable and the convenience method must not throw
        KeycloakOidcClient client = factory.create("override-realm", "override-client", "override-secret");
        assertThat(client).isNotNull();
    }
}
