package com.github.gelald.keycloak.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.gelald.keycloak.config.KeycloakOidcProperties;
import com.github.gelald.keycloak.dto.*;
import com.github.gelald.keycloak.exception.KeycloakAccessDeniedException;
import com.github.gelald.keycloak.exception.KeycloakAuthenticationException;
import com.github.gelald.keycloak.exception.KeycloakOidcException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests using WireMock to simulate Keycloak server.
 */
class KeycloakOidcClientTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private KeycloakOidcClient client;

    @BeforeEach
    void setUp() {
        wireMock.resetAll();

        String baseUrl = wireMock.baseUrl();
        KeycloakOidcProperties properties = new KeycloakOidcProperties();
        properties.setDomain(baseUrl);
        properties.setRealm("test-realm");
        properties.setClientId("test-client");
        properties.setClientSecret("test-secret");

        client = new KeycloakOidcClient(properties, new ObjectMapper());
    }

    @Test
    @DisplayName("getTokenByClientCredentials - success")
    void getTokenByClientCredentials() {
        wireMock.stubFor(post(urlEqualTo("/realms/test-realm/protocol/openid-connect/token"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "access_token": "at_abc",
                                  "refresh_token": "rt_def",
                                  "id_token": "it_ghi",
                                  "expires_in": 300,
                                  "token_type": "Bearer",
                                  "scope": "openid"
                                }
                                """)));

        ClientCredentialsTokenRequest request = new ClientCredentialsTokenRequest();
        TokenResponse response = client.getTokenByClientCredentials(request);

        assertThat(response.getAccessToken()).isEqualTo("at_abc");
        assertThat(response.getRefreshToken()).isEqualTo("rt_def");
        assertThat(response.getExpiresIn()).isEqualTo(300);
        assertThat(response.getTokenType()).isEqualTo("Bearer");

        wireMock.verify(postRequestedFor(urlEqualTo("/realms/test-realm/protocol/openid-connect/token"))
                .withHeader("Authorization", equalTo("Basic " + Base64.getEncoder()
                        .encodeToString("test-client:test-secret".getBytes(StandardCharsets.UTF_8)))));
    }

    @Test
    @DisplayName("introspect - success")
    void introspect() {
        wireMock.stubFor(post(urlEqualTo("/realms/test-realm/protocol/openid-connect/token/introspect"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "active": true,
                                  "typ": "Bearer",
                                  "exp": 1700000000,
                                  "sub": "user-123",
                                  "username": "john"
                                }
                                """)));

        IntrospectRequest request = new IntrospectRequest();
        request.setToken("some-token");
        IntrospectResponse response = client.introspect(request);

        assertThat(response.getActive()).isTrue();
        assertThat(response.getSubject()).isEqualTo("user-123");
    }

    @Test
    @DisplayName("logout - success (204)")
    void logout() {
        wireMock.stubFor(post(urlEqualTo("/realms/test-realm/protocol/openid-connect/logout"))
                .willReturn(aResponse().withStatus(204)));

        LogoutRequest request = new LogoutRequest();
        request.setIdTokenHint("some-id-token");

        client.logout(request);
        wireMock.verify(postRequestedFor(urlEqualTo("/realms/test-realm/protocol/openid-connect/logout")));
    }

    @Test
    @DisplayName("revoke - success (204)")
    void revoke() {
        wireMock.stubFor(post(urlEqualTo("/realms/test-realm/protocol/openid-connect/revoke"))
                .willReturn(aResponse().withStatus(204)));

        RevokeRequest request = new RevokeRequest();
        request.setToken("some-access-token");

        client.revoke(request);
        wireMock.verify(postRequestedFor(urlEqualTo("/realms/test-realm/protocol/openid-connect/revoke")));
    }

    @Test
    @DisplayName("certs - returns JWKS")
    void certs() {
        wireMock.stubFor(get(urlEqualTo("/realms/test-realm/protocol/openid-connect/certs"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
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
                                """)));

        CertificateResponse response = client.certs();
        assertThat(response.getKeys()).hasSize(1);
    }

    @Test
    @DisplayName("ready - health check")
    void ready() {
        wireMock.stubFor(get(urlEqualTo("/health/ready"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\": \"UP\"}")));

        HealthResponse response = client.ready();
        assertThat(response.getStatus()).isEqualTo("UP");
    }

    @Test
    @DisplayName("live - health check")
    void live() {
        wireMock.stubFor(get(urlEqualTo("/health/live"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\": \"UP\"}")));

        HealthResponse response = client.live();
        assertThat(response.getStatus()).isEqualTo("UP");
    }

    @Test
    @DisplayName("getTokenByAuthCode - success")
    void getTokenByAuthCode() {
        wireMock.stubFor(post(urlEqualTo("/realms/test-realm/protocol/openid-connect/token"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "access_token": "at_auth_code",
                                  "refresh_token": "rt_auth_code",
                                  "expires_in": 3600,
                                  "token_type": "Bearer"
                                }
                                """)));

        AuthCodeTokenRequest request = new AuthCodeTokenRequest();
        request.setAuthCode("code_xyz");
        request.setRedirectUri("https://example.com/cb");
        request.setCodeVerifier("verifier_abc");
        TokenResponse response = client.getTokenByAuthCode(request);

        assertThat(response.getAccessToken()).isEqualTo("at_auth_code");
        assertThat(response.getRefreshToken()).isEqualTo("rt_auth_code");
        assertThat(response.getExpiresIn()).isEqualTo(3600);

        wireMock.verify(postRequestedFor(urlEqualTo("/realms/test-realm/protocol/openid-connect/token"))
                .withRequestBody(containing("grant_type=authorization_code"))
                .withRequestBody(containing("code=code_xyz"))
                .withRequestBody(containing("code_verifier=verifier_abc")));
    }

    @Test
    @DisplayName("getTokenByRefresh - success")
    void getTokenByRefresh() {
        wireMock.stubFor(post(urlEqualTo("/realms/test-realm/protocol/openid-connect/token"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "access_token": "at_refreshed",
                                  "refresh_token": "rt_refreshed",
                                  "expires_in": 1800,
                                  "token_type": "Bearer"
                                }
                                """)));

        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("old-refresh-token");
        TokenResponse response = client.getTokenByRefresh(request);

        assertThat(response.getAccessToken()).isEqualTo("at_refreshed");
        assertThat(response.getRefreshToken()).isEqualTo("rt_refreshed");

        wireMock.verify(postRequestedFor(urlEqualTo("/realms/test-realm/protocol/openid-connect/token"))
                .withRequestBody(containing("grant_type=refresh_token"))
                .withRequestBody(containing("refresh_token=old-refresh-token")));
    }

    @Test
    @DisplayName("getTokenByDirectFlow - success")
    void getTokenByDirectFlow() {
        wireMock.stubFor(post(urlEqualTo("/realms/test-realm/protocol/openid-connect/token"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "access_token": "at_password",
                                  "refresh_token": "rt_password",
                                  "expires_in": 300,
                                  "token_type": "Bearer"
                                }
                                """)));

        DirectTokenRequest request = new DirectTokenRequest();
        request.setUsername("johndoe");
        request.setPassword("secret");
        request.setScope("openid profile");
        TokenResponse response = client.getTokenByDirectFlow(request);

        assertThat(response.getAccessToken()).isEqualTo("at_password");
        assertThat(response.getRefreshToken()).isEqualTo("rt_password");

        wireMock.verify(postRequestedFor(urlEqualTo("/realms/test-realm/protocol/openid-connect/token"))
                .withRequestBody(containing("grant_type=password"))
                .withRequestBody(containing("username=johndoe"))
                .withRequestBody(containing("password=secret"))
                .withRequestBody(containing("scope=openid+profile")));
    }

    @Test
    @DisplayName("error 400 on token endpoint -> KeycloakOidcException")
    void error400OnToken() {
        wireMock.stubFor(post(urlEqualTo("/realms/test-realm/protocol/openid-connect/token"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"invalid_grant\",\"error_description\":\"Invalid user credentials\"}")));

        ClientCredentialsTokenRequest request = new ClientCredentialsTokenRequest();

        assertThatThrownBy(() -> client.getTokenByClientCredentials(request))
                .isInstanceOf(KeycloakOidcException.class);
    }

    @Test
    @DisplayName("error 401 on token endpoint -> KeycloakAuthenticationException")
    void error401OnToken() {
        wireMock.stubFor(post(urlEqualTo("/realms/test-realm/protocol/openid-connect/token"))
                .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"invalid_client\",\"error_description\":\"Invalid client credentials\"}")));

        ClientCredentialsTokenRequest request = new ClientCredentialsTokenRequest();

        assertThatThrownBy(() -> client.getTokenByClientCredentials(request))
                .isInstanceOf(KeycloakAuthenticationException.class);
    }

    @Test
    @DisplayName("error 403 on token endpoint -> KeycloakAccessDeniedException")
    void error403OnToken() {
        wireMock.stubFor(post(urlEqualTo("/realms/test-realm/protocol/openid-connect/token"))
                .willReturn(aResponse()
                        .withStatus(403)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"access_denied\",\"error_description\":\"Not allowed\"}")));

        ClientCredentialsTokenRequest request = new ClientCredentialsTokenRequest();

        assertThatThrownBy(() -> client.getTokenByClientCredentials(request))
                .isInstanceOf(KeycloakAccessDeniedException.class);
    }

    @Test
    @DisplayName("error 500 on certs endpoint -> KeycloakOidcException")
    void error500OnCerts() {
        wireMock.stubFor(get(urlEqualTo("/realms/test-realm/protocol/openid-connect/certs"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"server_error\",\"error_description\":\"Internal server error\"}")));

        assertThatThrownBy(() -> client.certs())
                .isInstanceOf(KeycloakOidcException.class);
    }

    @Test
    @DisplayName("error 500 on health endpoints -> KeycloakOidcException")
    void error500OnHealth() {
        wireMock.stubFor(get(urlEqualTo("/health/ready"))
                .willReturn(aResponse().withStatus(500)));
        wireMock.stubFor(get(urlEqualTo("/health/live"))
                .willReturn(aResponse().withStatus(500)));

        assertThatThrownBy(() -> client.ready()).isInstanceOf(KeycloakOidcException.class);
        assertThatThrownBy(() -> client.live()).isInstanceOf(KeycloakOidcException.class);
    }

    @Test
    @DisplayName("error 204 on revoke with empty body -> no exception")
    void revokeWith204AndEmptyBody() {
        wireMock.stubFor(post(urlEqualTo("/realms/test-realm/protocol/openid-connect/revoke"))
                .willReturn(aResponse().withStatus(204)));

        RevokeRequest request = new RevokeRequest();
        request.setToken("some-token");

        client.revoke(request);
        wireMock.verify(postRequestedFor(urlEqualTo("/realms/test-realm/protocol/openid-connect/revoke")));
    }

    @Test
    @DisplayName("authorizationUrl - builds correct URL with PKCE params")
    void authorizationUrl() {
        String url = client.authorizationUrl(
                "https://myapp.com/callback",
                "state-123",
                "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM");

        String base = wireMock.baseUrl();
        assertThat(url).startsWith(base + "/realms/test-realm/protocol/openid-connect/auth");
        assertThat(url).contains("response_type=code");
        assertThat(url).contains("client_id=test-client");
        assertThat(url).contains("redirect_uri=https://myapp.com/callback");
        assertThat(url).contains("state=state-123");
        assertThat(url).contains("code_challenge=E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM");
        assertThat(url).contains("code_challenge_method=S256");
        assertThat(url).contains("scope=openid");
    }
}