package com.github.gelald.keycloak;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.gelald.keycloak.client.KeycloakOidcClient;
import com.github.gelald.keycloak.client.KeycloakOidcClientFactory;
import com.github.gelald.keycloak.config.KeycloakOidcProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestClient;

/**
 * Keycloak OIDC 客户端自动配置。
 * <p>
 * 当 {@code keycloak.oidc.enabled} 未设置为 {@code false} 时激活。
 * <p>
 * 用户可通过提供自定义的 {@link RestClient.Builder} Bean 来定制 SSL、代理、拦截器等。
 *
 * @since 1.0
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "keycloak.oidc", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(value = KeycloakOidcProperties.class)
public class KeycloakOidcAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public KeycloakOidcClient keycloakOidcClient(KeycloakOidcProperties properties,
                                                  ObjectMapper objectMapper,
                                                  @Nullable RestClient.Builder restClientBuilder) {
        return new KeycloakOidcClient(properties, objectMapper, restClientBuilder);
    }

    @Bean
    @ConditionalOnMissingBean
    public KeycloakOidcClientFactory keycloakOidcClientFactory(KeycloakOidcProperties properties,
                                                               ObjectMapper objectMapper) {
        return new KeycloakOidcClientFactory(properties, objectMapper);
    }
}
