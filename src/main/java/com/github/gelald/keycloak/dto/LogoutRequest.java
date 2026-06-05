package com.github.gelald.keycloak.dto;

import lombok.Data;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Logout request to end an OIDC session.
 *
* @see <a href="https://openid.net/specs/openid-connect-session-1_0.html">OpenID Connect Session Management</a>
 */
@Data
public class LogoutRequest {
    /**
     * ID token hint to identify the session to end.
     * <br/>
     * required
     */
    private String idTokenHint;
    /**
     * URI to redirect to after logout.
     * <br/>
     * optional
     */
    private String postLogoutRedirectUri;

    public MultiValueMap<String, String> toMultiValueMap() {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        if (idTokenHint != null) map.add("id_token_hint", idTokenHint);
        if (postLogoutRedirectUri != null) map.add("post_logout_redirect_uri", postLogoutRedirectUri);
        return map;
    }
}
