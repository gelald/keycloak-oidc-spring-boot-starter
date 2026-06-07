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
    /** RFC 6749 parameter: {@code access_token}. The access token issued by the authorization server. */
    @JsonProperty("access_token")
    private String accessToken;
    /** RFC 6749 parameter: {@code refresh_token}. The refresh token used to obtain new access tokens. */
    @JsonProperty("refresh_token")
    private String refreshToken;
    /** OIDC parameter: {@code id_token}. The ID Token value associated with the authenticated session. */
    @JsonProperty("id_token")
    private String idToken;
    /** RFC 6749 parameter: {@code expires_in}. The lifetime in seconds of the access token. */
    @JsonProperty("expires_in")
    private Long expiresIn;
    /** RFC 6749 parameter: {@code token_type}. The type of the token issued (typically "Bearer"). */
    @JsonProperty("token_type")
    private String tokenType;
    /** RFC 6749 parameter: {@code scope}. The scope of the access token. */
    @JsonProperty("scope")
    private String scope;
    /** Keycloak-specific parameter: {@code session_state}. Identifies the user's session. */
    @JsonProperty("session_state")
    private String sessionState;
    /** Keycloak-specific parameter: {@code not-before-policy}. Timestamp (seconds since epoch) before which the token is not valid. */
    @JsonProperty("not-before-policy")
    private Long notBeforePolicy;
}
