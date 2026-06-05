package com.github.gelald.keycloak.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * OAuth2 / OIDC token endpoint response.
 *
* @see <a href="https://tools.ietf.org/html/rfc6749#section-5.1">RFC 6749 - Access Token Response</a>
 */
@Data
public class TokenResponse {
    /**
     * The access token issued by the authorization server.
     */
    @JsonProperty("access_token")
    private String accessToken;
    /**
     * The refresh token, which can be used to obtain new access tokens.
     */
    @JsonProperty("refresh_token")
    private String refreshToken;
    /**
     * ID Token value associated with the authenticated session (OIDC).
     */
    @JsonProperty("id_token")
    private String idToken;
    /**
     * The lifetime in seconds of the access token.
     */
    @JsonProperty("expires_in")
    private Long expiresIn;
    /**
     * The type of the token issued (always "Bearer").
     */
    @JsonProperty("token_type")
    private String tokenType;
    /**
     * The scope of the access token.
     */
    @JsonProperty("scope")
    private String scope;
    /**
     * Session state (Keycloak-specific).
     */
    @JsonProperty("session_state")
    private String sessionState;
    /**
     * Not-before policy timestamp (Keycloak-specific).
     */
    @JsonProperty("not-before-policy")
    private Long notBeforePolicy;
}
