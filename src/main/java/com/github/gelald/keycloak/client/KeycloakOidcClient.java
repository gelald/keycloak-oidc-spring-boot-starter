package com.github.gelald.keycloak.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.gelald.keycloak.config.KeycloakOidcProperties;
import com.github.gelald.keycloak.dto.*;
import com.github.gelald.keycloak.exception.KeycloakErrorDecoder;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;

/**
 * Keycloak OIDC client covering OpenID Connect / OAuth2 protocol endpoints.
 * Uses Spring's {@link RestClient} internally — no Feign / Spring Cloud dependency.
 *
* @see <a href="https://www.keycloak.org/docs/latest/securing_apps/">Keycloak Securing Applications</a>
 */
public class KeycloakOidcClient {

    private final RestClient restClient;
    private final KeycloakOidcProperties properties;
    private final KeycloakErrorDecoder errorDecoder;

    public KeycloakOidcClient(KeycloakOidcProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.errorDecoder = new KeycloakErrorDecoder(objectMapper);

        String basicAuth = "Basic " + java.util.Base64.getEncoder()
                .encodeToString((properties.getClientId() + ":" + properties.getClientSecret())
                        .getBytes(StandardCharsets.UTF_8));

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory();
        requestFactory.setReadTimeout(30_000);

        this.restClient = RestClient.builder()
                .baseUrl(properties.getDomain())
                .defaultHeader(org.springframework.http.HttpHeaders.AUTHORIZATION, basicAuth)
                .requestFactory(requestFactory)
                .build();
    }

    /**
     * Introspect a token to check if it is active.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7662">RFC 7662 - OAuth 2.0 Token Introspection</a>
     */
    public IntrospectResponse introspect(IntrospectRequest introspectRequest) {
        return postForm(
                "/realms/{realm}/protocol/openid-connect/token/introspect",
                introspectRequest.toMultiValueMap(),
                IntrospectResponse.class);
    }

    /**
     * Exchange an authorization code for tokens (Authorization Code Flow with PKCE).
     *
     * @see <a href="https://tools.ietf.org/html/rfc6749#section-4.1">RFC 6749 - Authorization Code Grant</a>
     */
    public TokenResponse getTokenByAuthCode(AuthCodeTokenRequest authCodeTokenRequest) {
        return postForm(
                "/realms/{realm}/protocol/openid-connect/token",
                authCodeTokenRequest.toMultiValueMap(),
                TokenResponse.class);
    }

    /**
     * Refresh an expired access token using a refresh token.
     *
     * @see <a href="https://tools.ietf.org/html/rfc6749#section-6">RFC 6749 - Refreshing an Access Token</a>
     */
    public TokenResponse getTokenByRefresh(RefreshTokenRequest refreshTokenRequest) {
        return postForm(
                "/realms/{realm}/protocol/openid-connect/token",
                refreshTokenRequest.toMultiValueMap(),
                TokenResponse.class);
    }

    /**
     * Obtain a token using client credentials (machine-to-machine).
     *
     * @see <a href="https://tools.ietf.org/html/rfc6749#section-4.4">RFC 6749 - Client Credentials Grant</a>
     */
    public TokenResponse getTokenByClientCredentials(ClientCredentialsTokenRequest clientCredentialsTokenRequest) {
        return postForm(
                "/realms/{realm}/protocol/openid-connect/token",
                clientCredentialsTokenRequest.toMultiValueMap(),
                TokenResponse.class);
    }

    /**
     * Obtain a token using username and password (Resource Owner Password Credentials).
     *
     * @see <a href="https://tools.ietf.org/html/rfc6749#section-4.3">RFC 6749 - Resource Owner Password Credentials Grant</a>
     */
    public TokenResponse getTokenByDirectFlow(DirectTokenRequest directTokenRequest) {
        return postForm(
                "/realms/{realm}/protocol/openid-connect/token",
                directTokenRequest.toMultiValueMap(),
                TokenResponse.class);
    }

    /**
     * End a session using the id_token_hint.
     *
     * @see <a href="https://openid.net/specs/openid-connect-session-1_0.html">OpenID Connect Session Management</a>
     */
    public void logout(LogoutRequest logoutRequest) {
        postFormVoid(
                "/realms/{realm}/protocol/openid-connect/logout",
                logoutRequest.toMultiValueMap());
    }

    /**
     * Revoke an access token.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7009">RFC 7009 - OAuth 2.0 Token Revocation</a>
     */
    public void revoke(RevokeRequest revokeRequest) {
        postFormVoid(
                "/realms/{realm}/protocol/openid-connect/revoke",
                revokeRequest.toMultiValueMap());
    }

    /**
     * Retrieve JWKS public keys for JWT signature verification.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7517">RFC 7517 - JSON Web Key (JWK)</a>
     */
    public CertificateResponse certs() {
        return getJson(
                "/realms/{realm}/protocol/openid-connect/certs",
                CertificateResponse.class);
    }

    /**
     * Check Keycloak readiness (database and other dependencies).
     */
    public HealthResponse ready() {
        return getJson("/health/ready", HealthResponse.class);
    }

    /**
     * Check Keycloak liveness.
     */
    public HealthResponse live() {
        return getJson("/health/live", HealthResponse.class);
    }

    /**
     * Build the Keycloak authorization URL for Browser Flow (Authorization Code + PKCE).
     * <p>
     * The caller should redirect the end-user's browser to this URL.
     * After authentication, Keycloak redirects back with an authorization code,
     * which can be exchanged via {@link #getTokenByAuthCode(AuthCodeTokenRequest)}.
     *
     * @param redirectUri   the redirect URI registered in Keycloak
     * @param state         CSRF / session state value
     * @param codeChallenge PKCE code challenge (generated via {@link com.github.gelald.keycloak.util.PkceUtils#generateCodeChallengeS256(String)})
     * @return the full authorization URL
     */
    public String authorizationUrl(String redirectUri, String state, String codeChallenge) {
        String authDomain = properties.getPublicDomain() != null ? properties.getPublicDomain() : properties.getDomain();
        return org.springframework.web.util.UriComponentsBuilder
                .fromHttpUrl(authDomain)
                .path("/realms/{realm}/protocol/openid-connect/auth")
                .queryParam("response_type", "code")
                .queryParam("client_id", properties.getClientId())
                .queryParam("redirect_uri", redirectUri)
                .queryParam("state", state)
                .queryParam("code_challenge", codeChallenge)
                .queryParam("code_challenge_method", "S256")
                .queryParam("scope", "openid")
                .build(properties.getRealm())
                .toString();
    }

    private <T> T postForm(String uri, org.springframework.util.MultiValueMap<String, String> formBody, Class<T> responseType) {
        return restClient.post()
                .uri(uri, properties.getRealm())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formBody)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, resp) -> {
                    String body = new String(resp.getBody().readAllBytes(), StandardCharsets.UTF_8);
                    throw errorDecoder.decode(resp.getStatusCode().value(), body);
                })
                .body(responseType);
    }

    private void postFormVoid(String uri, org.springframework.util.MultiValueMap<String, String> formBody) {
        restClient.post()
                .uri(uri, properties.getRealm())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formBody)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, resp) -> {
                    String body = new String(resp.getBody().readAllBytes(), StandardCharsets.UTF_8);
                    throw errorDecoder.decode(resp.getStatusCode().value(), body);
                })
                .toBodilessEntity();
    }

    private <T> T getJson(String uri, Class<T> responseType) {
        return restClient.get()
                .uri(uri, properties.getRealm())
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, resp) -> {
                    String body = new String(resp.getBody().readAllBytes(), StandardCharsets.UTF_8);
                    throw errorDecoder.decode(resp.getStatusCode().value(), body);
                })
                .body(responseType);
    }
}