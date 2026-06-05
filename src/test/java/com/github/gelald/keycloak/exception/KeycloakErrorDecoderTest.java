package com.github.gelald.keycloak.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * KeycloakErrorDecoder unit tests.
 */
class KeycloakErrorDecoderTest {

    private final KeycloakErrorDecoder decoder = new KeycloakErrorDecoder(new ObjectMapper());

    @Test
    @DisplayName("401 response -> KeycloakAuthenticationException")
    void decode401() {
        RuntimeException ex = decoder.decode(401,
                "{\"error\":\"invalid_client\",\"error_description\":\"Invalid client credentials\"}");

        assertThat(ex).isInstanceOf(KeycloakAuthenticationException.class);
        KeycloakAuthenticationException auth = (KeycloakAuthenticationException) ex;
        assertThat(auth.getError()).isEqualTo("invalid_client");
        assertThat(auth.getDescription()).isEqualTo("Invalid client credentials");
        assertThat(auth.getStatus()).isEqualTo(401);
    }

    @Test
    @DisplayName("403 response -> KeycloakAccessDeniedException")
    void decode403() {
        RuntimeException ex = decoder.decode(403,
                "{\"error\":\"access_denied\",\"error_description\":\"Not allowed\"}");

        assertThat(ex).isInstanceOf(KeycloakAccessDeniedException.class);
        KeycloakAccessDeniedException denied = (KeycloakAccessDeniedException) ex;
        assertThat(denied.getError()).isEqualTo("access_denied");
    }

    @Test
    @DisplayName("400 response -> KeycloakOidcException")
    void decode400() {
        RuntimeException ex = decoder.decode(400,
                "{\"error\":\"invalid_grant\",\"error_description\":\"Invalid user credentials\"}");

        assertThat(ex).isInstanceOf(KeycloakOidcException.class);
        KeycloakOidcException oidc = (KeycloakOidcException) ex;
        assertThat(oidc.getStatus()).isEqualTo(400);
        assertThat(oidc.getError()).isEqualTo("invalid_grant");
    }

    @Test
    @DisplayName("Empty body -> falls back to default values")
    void decodeEmptyBody() {
        RuntimeException ex = decoder.decode(500, "");

        assertThat(ex).isInstanceOf(KeycloakOidcException.class);
        KeycloakOidcException oidc = (KeycloakOidcException) ex;
        assertThat(oidc.getStatus()).isEqualTo(500);
        assertThat(oidc.getDescription()).isEqualTo("Unknown error");
    }

    @Test
    @DisplayName("Null body -> falls back to default values")
    void decodeNullBody() {
        RuntimeException ex = decoder.decode(500, null);

        assertThat(ex).isInstanceOf(KeycloakOidcException.class);
        KeycloakOidcException oidc = (KeycloakOidcException) ex;
        assertThat(oidc.getStatus()).isEqualTo(500);
    }
}