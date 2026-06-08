package com.github.gelald.keycloak.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.gelald.keycloak.config.KeycloakOidcProperties;
import org.springframework.beans.BeanUtils;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestClient;

/**
 * Factory for creating {@link KeycloakOidcClient} instances targeting different realms or configurations.
 * <p>
 * Useful in multi-realm scenarios where a single application needs to interact with
 * multiple Keycloak realms, each with its own client ID and secret.
 *
 * @since 1.1
 */
public class KeycloakOidcClientFactory {

    private final KeycloakOidcProperties defaultProperties;
    private final ObjectMapper objectMapper;
    @Nullable
    private final RestClient.Builder restClientBuilder;

    /**
     * @param defaultProperties  the application-wide default properties (used as a template)
     * @param objectMapper       JSON serialization/deserialization
     * @param restClientBuilder  optional {@link RestClient.Builder} for custom SSL, proxy, interceptors
     */
    public KeycloakOidcClientFactory(KeycloakOidcProperties defaultProperties,
                                    ObjectMapper objectMapper,
                                    @Nullable RestClient.Builder restClientBuilder) {
        this.defaultProperties = defaultProperties;
        this.objectMapper = objectMapper;
        this.restClientBuilder = restClientBuilder;
    }

    /**
     * Create a client for a specific realm with the given credentials.
     * <p>
     * All other properties (domain, publicDomain, timeouts, etc.) are inherited from the
     * application-wide default properties.
     *
     * @param realm        the Keycloak realm name
     * @param clientId     the OAuth2 client ID
     * @param clientSecret the OAuth2 client secret
     * @return a new {@link KeycloakOidcClient} configured for the specified realm
     */
    public KeycloakOidcClient create(String realm, String clientId, String clientSecret) {
        KeycloakOidcProperties props = copyWithOverrides(defaultProperties, realm, clientId, clientSecret);
        return new KeycloakOidcClient(props, objectMapper, restClientBuilder);
    }

    /**
     * Create a client with fully custom properties.
     *
     * @param properties the complete set of Keycloak OIDC properties
     * @return a new {@link KeycloakOidcClient} configured with the given properties
     */
    public KeycloakOidcClient create(KeycloakOidcProperties properties) {
        return new KeycloakOidcClient(properties, objectMapper, restClientBuilder);
    }

    /**
     * Create a shallow copy of the base properties with only realm, clientId, and clientSecret overridden.
     * <p>
     * Uses {@link BeanUtils#copyProperties} to automatically copy all matching fields,
     * so new properties added to {@link KeycloakOidcProperties} won't be silently dropped.
     *
     * @return a new {@link KeycloakOidcProperties} with realm, clientId, clientSecret overridden
     */
    static KeycloakOidcProperties copyWithOverrides(
            KeycloakOidcProperties base, String realm, String clientId, String clientSecret) {
        KeycloakOidcProperties copy = new KeycloakOidcProperties();
        BeanUtils.copyProperties(base, copy);
        copy.setRealm(realm);
        copy.setClientId(clientId);
        copy.setClientSecret(clientSecret);
        return copy;
    }
}
