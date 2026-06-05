package com.github.gelald.keycloak.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.gelald.keycloak.enums.GrantTypeEnum;

import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DTO serialization / deserialization tests.
 */
class DtoSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("TokenResponse - deserialize all fields")
    void tokenResponseDeserialize() throws Exception {
        String json = """
                {
                  "access_token": "at_123",
                  "refresh_token": "rt_456",
                  "id_token": "it_789",
                  "expires_in": 300,
                  "token_type": "Bearer",
                  "scope": "openid profile",
                  "session_state": "ss_abc",
                  "not-before-policy": 0
                }
                """;
        TokenResponse resp = objectMapper.readValue(json, TokenResponse.class);

        assertThat(resp.getAccessToken()).isEqualTo("at_123");
        assertThat(resp.getRefreshToken()).isEqualTo("rt_456");
        assertThat(resp.getIdToken()).isEqualTo("it_789");
        assertThat(resp.getExpiresIn()).isEqualTo(300);
        assertThat(resp.getTokenType()).isEqualTo("Bearer");
        assertThat(resp.getScope()).isEqualTo("openid profile");
        assertThat(resp.getSessionState()).isEqualTo("ss_abc");
        assertThat(resp.getNotBeforePolicy()).isEqualTo(0);
    }

    @Test
    @DisplayName("IntrospectResponse - deserialize all fields")
    void introspectResponseDeserialize() throws Exception {
        String json = """
                {
                  "active": true,
                  "typ": "Bearer",
                  "exp": 1700000000,
                  "sub": "user-123",
                  "username": "john",
                  "aud": "my-client",
                  "iss": "https://keycloak.example.com/realms/test",
                  "iat": 1699999000,
                  "scope": "openid profile email",
                  "client_id": "my-client"
                }
                """;
        IntrospectResponse resp = objectMapper.readValue(json, IntrospectResponse.class);

        assertThat(resp.getActive()).isTrue();
        assertThat(resp.getType()).isEqualTo("Bearer");
        assertThat(resp.getExpire()).isEqualTo(1700000000L);
        assertThat(resp.getSubject()).isEqualTo("user-123");
        assertThat(resp.getUsername()).isEqualTo("john");
        assertThat(resp.getAudience()).isEqualTo("my-client");
        assertThat(resp.getIssuer()).contains("keycloak.example.com");
        assertThat(resp.getIssuedAt()).isEqualTo(1699999000L);
        assertThat(resp.getScope()).isEqualTo("openid profile email");
        assertThat(resp.getClientId()).isEqualTo("my-client");
    }

    @Test
    @DisplayName("HealthResponse - deserialize")
    void healthResponseDeserialize() throws Exception {
        String json = """
                {
                  "status": "UP"
                }
                """;
        HealthResponse resp = objectMapper.readValue(json, HealthResponse.class);
        assertThat(resp.getStatus()).isEqualTo("UP");
    }

    @Test
    @DisplayName("GrantTypeEnum - values")
    void grantTypeEnumValues() {
        assertThat(GrantTypeEnum.AUTH_CODE.getValue()).isEqualTo("authorization_code");
        assertThat(GrantTypeEnum.CLIENT.getValue()).isEqualTo("client_credentials");
        assertThat(GrantTypeEnum.PASSWORD.getValue()).isEqualTo("password");
        assertThat(GrantTypeEnum.REFRESH.getValue()).isEqualTo("refresh_token");
    }

    @Test
    @DisplayName("Request DTOs - default grant types")
    void requestDefaults() {
        assertThat(new AuthCodeTokenRequest().getGrantType()).isEqualTo("authorization_code");
        assertThat(new ClientCredentialsTokenRequest().getGrantType()).isEqualTo("client_credentials");
        assertThat(new DirectTokenRequest().getGrantType()).isEqualTo("password");
        assertThat(new RefreshTokenRequest().getGrantType()).isEqualTo("refresh_token");
    }

    @Test
    @DisplayName("IntrospectRequest - default token_type_hint is access_token")
    void introspectRequestDefault() {
        assertThat(new IntrospectRequest().getTokenTypeHint()).isEqualTo("access_token");
    }

    @Test
    @DisplayName("RevokeRequest - default token_type_hint is access_token")
    void revokeRequestDefault() {
        assertThat(new RevokeRequest().getTokenTypeHint()).isEqualTo("access_token");
    }

    @Test
    @DisplayName("ClientCredentialsTokenRequest - toMultiValueMap")
    void clientCredentialsToForm() {
        ClientCredentialsTokenRequest req = new ClientCredentialsTokenRequest();
        req.setScope("openid profile");
        var form = req.toMultiValueMap();
        assertThat(form.getFirst("grant_type")).isEqualTo("client_credentials");
        assertThat(form.getFirst("scope")).isEqualTo("openid profile");
    }

    @Test
    @DisplayName("AuthCodeTokenRequest - toMultiValueMap")
    void authCodeToForm() {
        AuthCodeTokenRequest req = new AuthCodeTokenRequest();
        req.setAuthCode("code123");
        req.setRedirectUri("https://example.com/cb");
        req.setCodeVerifier("verifier123");
        var form = req.toMultiValueMap();
        assertThat(form.getFirst("grant_type")).isEqualTo("authorization_code");
        assertThat(form.getFirst("code")).isEqualTo("code123");
        assertThat(form.getFirst("redirect_uri")).isEqualTo("https://example.com/cb");
        assertThat(form.getFirst("code_verifier")).isEqualTo("verifier123");
    }

    @Test
    @DisplayName("DirectTokenRequest - toMultiValueMap")
    void directTokenToForm() {
        DirectTokenRequest req = new DirectTokenRequest();
        req.setUsername("johndoe");
        req.setPassword("p@ssword");
        req.setScope("openid profile");
        var form = req.toMultiValueMap();
        assertThat(form.getFirst("grant_type")).isEqualTo("password");
        assertThat(form.getFirst("username")).isEqualTo("johndoe");
        assertThat(form.getFirst("password")).isEqualTo("p@ssword");
        assertThat(form.getFirst("scope")).isEqualTo("openid profile");
    }

    @Test
    @DisplayName("DirectTokenRequest - toMultiValueMap with extParams")
    void directTokenToFormWithExtParams() {
        DirectTokenRequest req = new DirectTokenRequest();
        req.setUsername("johndoe");
        req.setPassword("p@ssword");
        req.setExtParams(Map.of("custom_param", "custom_value"));
        var form = req.toMultiValueMap();
        assertThat(form.getFirst("custom_param")).isEqualTo("custom_value");
    }

    @Test
    @DisplayName("RefreshTokenRequest - toMultiValueMap")
    void refreshTokenToForm() {
        RefreshTokenRequest req = new RefreshTokenRequest();
        req.setRefreshToken("old-refresh-token");
        var form = req.toMultiValueMap();
        assertThat(form.getFirst("grant_type")).isEqualTo("refresh_token");
        assertThat(form.getFirst("refresh_token")).isEqualTo("old-refresh-token");
    }

    @Test
    @DisplayName("RefreshTokenRequest - toMultiValueMap with extParams")
    void refreshTokenToFormWithExtParams() {
        RefreshTokenRequest req = new RefreshTokenRequest();
        req.setRefreshToken("old-refresh-token");
        req.setExtParams(Map.of("custom_param", "custom_value"));
        var form = req.toMultiValueMap();
        assertThat(form.getFirst("custom_param")).isEqualTo("custom_value");
    }

    @Test
    @DisplayName("IntrospectRequest - toMultiValueMap")
    void introspectToForm() {
        IntrospectRequest req = new IntrospectRequest();
        req.setToken("some-token");
        req.setTokenTypeHint("refresh_token");
        var form = req.toMultiValueMap();
        assertThat(form.getFirst("token")).isEqualTo("some-token");
        assertThat(form.getFirst("token_type_hint")).isEqualTo("refresh_token");
    }

    @Test
    @DisplayName("LogoutRequest - toMultiValueMap")
    void logoutToForm() {
        LogoutRequest req = new LogoutRequest();
        req.setIdTokenHint("id-token-hint");
        req.setPostLogoutRedirectUri("https://example.com/post-logout");
        var form = req.toMultiValueMap();
        assertThat(form.getFirst("id_token_hint")).isEqualTo("id-token-hint");
        assertThat(form.getFirst("post_logout_redirect_uri")).isEqualTo("https://example.com/post-logout");
    }

    @Test
    @DisplayName("RevokeRequest - toMultiValueMap")
    void revokeToForm() {
        RevokeRequest req = new RevokeRequest();
        req.setToken("some-access-token");
        req.setTokenTypeHint("refresh_token");
        var form = req.toMultiValueMap();
        assertThat(form.getFirst("token")).isEqualTo("some-access-token");
        assertThat(form.getFirst("token_type_hint")).isEqualTo("refresh_token");
    }

    @Test
    @DisplayName("CertificateResponse - deserialize")
    void certificateResponseDeserialize() throws Exception {
        String json = """
                {
                  "keys": [
                    {
                      "kid": "key-1",
                      "kty": "RSA",
                      "alg": "RS256",
                      "use": "sig",
                      "n": "modulus123",
                      "e": "AQAB"
                    }
                  ]
                }
                """;
        CertificateResponse resp = objectMapper.readValue(json, CertificateResponse.class);
        assertThat(resp.getKeys()).hasSize(1);
    }

    @Test
    @DisplayName("KeycloakErrorResponse - deserialize")
    void keycloakErrorResponseDeserialize() throws Exception {
        String json = """
                {
                  "error": "invalid_grant",
                  "error_description": "Invalid user credentials"
                }
                """;
        com.github.gelald.keycloak.exception.KeycloakErrorResponse resp =
                objectMapper.readValue(json, com.github.gelald.keycloak.exception.KeycloakErrorResponse.class);
        assertThat(resp.getError()).isEqualTo("invalid_grant");
        assertThat(resp.getErrorDescription()).isEqualTo("Invalid user credentials");
    }
}