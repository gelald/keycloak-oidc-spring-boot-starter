package com.github.gelald.keycloak.dto;

import lombok.Data;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Token revocation request (RFC 7009).
 *
* @see <a href="https://tools.ietf.org/html/rfc7009">RFC 7009 - OAuth 2.0 Token Revocation</a>
 */
@Data
public class RevokeRequest {
    /**
     * Hint about the type of the token being revoked (defaults to "access_token").
     * <br/>
     * optional
     */
    private String tokenTypeHint = "access_token";
    /**
     * The token to revoke.
     * <br/>
     * required
     */
    private String token;

    public MultiValueMap<String, String> toMultiValueMap() {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        if (token != null) map.add("token", token);
        if (tokenTypeHint != null) map.add("token_type_hint", tokenTypeHint);
        return map;
    }
}
