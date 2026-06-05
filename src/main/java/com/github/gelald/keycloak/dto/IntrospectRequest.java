package com.github.gelald.keycloak.dto;

import lombok.Data;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Token introspection request (RFC 7662).
 *
* @see <a href="https://tools.ietf.org/html/rfc7662">RFC 7662 - OAuth 2.0 Token Introspection</a>
 */
@Data
public class IntrospectRequest {
    /**
     * Hint about the type of the submitted token (defaults to "access_token").
     * <br/>
     * optional - helps the authorization server optimize the lookup
     */
    private String tokenTypeHint = "access_token";
    /**
     * The token to introspect.
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
