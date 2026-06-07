package com.github.gelald.keycloak.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Token revocation request (RFC 7009).
 *
* @see <a href="https://tools.ietf.org/html/rfc7009">RFC 7009 - OAuth 2.0 Token Revocation</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevokeRequest {
    /** RFC 7009 parameter: {@code token_type_hint}. Hint about the type of the token being revoked. Defaults to "access_token". Optional. */
    @Builder.Default
    private String tokenTypeHint = "access_token";
    /** RFC 7009 parameter: {@code token}. The token to revoke. Required. */
    private String token;

    public MultiValueMap<String, String> toMultiValueMap() {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        if (token != null) map.add("token", token);
        if (tokenTypeHint != null) map.add("token_type_hint", tokenTypeHint);
        return map;
    }
}
