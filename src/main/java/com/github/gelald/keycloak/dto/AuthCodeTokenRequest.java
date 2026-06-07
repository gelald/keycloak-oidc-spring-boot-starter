package com.github.gelald.keycloak.dto;

import com.github.gelald.keycloak.enums.GrantTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;

/**
 * Request for Authorization Code grant with PKCE support.
 *
 * @see <a href="https://tools.ietf.org/html/rfc6749#section-4.1">RFC 6749 - Authorization Code Grant</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthCodeTokenRequest {
    /** OAuth2 parameter: {@code grant_type}. Defaults to "authorization_code". Required. */
    @Builder.Default
    private String grantType = GrantTypeEnum.AUTH_CODE.getValue();
    /** OAuth2 parameter: {@code code}. The authorization code received from the authorization server. Required. */
    private String authCode;
    /** OAuth2 parameter: {@code redirect_uri}. Must match the URI used in the authorization request. Required. */
    private String redirectUri;
    /** PKCE parameter: {@code code_verifier}. The random string used to generate the code challenge. Required when using PKCE. */
    private String codeVerifier;
    /** Extension parameters for custom Keycloak SPI integrations. Optional — will be flattened into form parameters. */
    private Map<String, String> extParams;

    public MultiValueMap<String, String> toMultiValueMap() {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", grantType);
        if (authCode != null) map.add("code", authCode);
        if (redirectUri != null) map.add("redirect_uri", redirectUri);
        if (codeVerifier != null) map.add("code_verifier", codeVerifier);
        if (extParams != null) extParams.forEach(map::add);
        return map;
    }
}
