package com.github.gelald.keycloak.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Logout request to end an OIDC session.
 *
* @see <a href="https://openid.net/specs/openid-connect-session-1_0.html">OpenID Connect Session Management</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogoutRequest {
    /** OIDC parameter: {@code id_token_hint}. The ID token previously issued to the client. Required. */
    private String idTokenHint;
    /** OIDC parameter: {@code post_logout_redirect_uri}. URI to redirect the user agent after logout. Optional. */
    private String postLogoutRedirectUri;

    public MultiValueMap<String, String> toMultiValueMap() {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        if (idTokenHint != null) map.add("id_token_hint", idTokenHint);
        if (postLogoutRedirectUri != null) map.add("post_logout_redirect_uri", postLogoutRedirectUri);
        return map;
    }
}
