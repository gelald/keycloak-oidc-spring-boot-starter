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
        String body = "{\"error\":\"invalid_client\",\"error_description\":\"Invalid client credentials\"}";
        RuntimeException ex = decoder.decode(401, body);

        assertThat(ex).isInstanceOf(KeycloakAuthenticationException.class);
        KeycloakAuthenticationException auth = (KeycloakAuthenticationException) ex;
        assertThat(auth.getError()).isEqualTo("invalid_client");
        assertThat(auth.getDescription()).isEqualTo("Invalid client credentials");
        assertThat(auth.getStatus()).isEqualTo(401);
        assertThat(auth.getRawBody()).isEqualTo(body);
    }

    @Test
    @DisplayName("403 response -> KeycloakAccessDeniedException")
    void decode403() {
        String body = "{\"error\":\"access_denied\",\"error_description\":\"Not allowed\"}";
        RuntimeException ex = decoder.decode(403, body);

        assertThat(ex).isInstanceOf(KeycloakAccessDeniedException.class);
        KeycloakAccessDeniedException denied = (KeycloakAccessDeniedException) ex;
        assertThat(denied.getError()).isEqualTo("access_denied");
        assertThat(denied.getRawBody()).isEqualTo(body);
    }

    @Test
    @DisplayName("400 response -> KeycloakOidcException")
    void decode400() {
        String body = "{\"error\":\"invalid_grant\",\"error_description\":\"Invalid user credentials\"}";
        RuntimeException ex = decoder.decode(400, body);

        assertThat(ex).isInstanceOf(KeycloakOidcException.class);
        KeycloakOidcException oidc = (KeycloakOidcException) ex;
        assertThat(oidc.getStatus()).isEqualTo(400);
        assertThat(oidc.getError()).isEqualTo("invalid_grant");
        assertThat(oidc.getRawBody()).isEqualTo(body);
    }

    @Test
    @DisplayName("Empty body -> falls back to default values, rawBody is empty string")
    void decodeEmptyBody() {
        String body = "";
        RuntimeException ex = decoder.decode(500, body);

        assertThat(ex).isInstanceOf(KeycloakOidcException.class);
        KeycloakOidcException oidc = (KeycloakOidcException) ex;
        assertThat(oidc.getStatus()).isEqualTo(500);
        assertThat(oidc.getDescription()).isEqualTo("Unknown error");
        assertThat(oidc.getRawBody()).isEqualTo(body);
    }

    @Test
    @DisplayName("Null body -> falls back to default values, rawBody is null")
    void decodeNullBody() {
        RuntimeException ex = decoder.decode(500, null);

        assertThat(ex).isInstanceOf(KeycloakOidcException.class);
        KeycloakOidcException oidc = (KeycloakOidcException) ex;
        assertThat(oidc.getStatus()).isEqualTo(500);
        assertThat(oidc.getRawBody()).isNull();
    }
}
