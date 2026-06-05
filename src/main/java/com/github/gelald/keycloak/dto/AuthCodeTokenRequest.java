package com.github.gelald.keycloak.dto;

import com.github.gelald.keycloak.enums.GrantTypeEnum;
import lombok.Data;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;

/**
 * Request for Authorization Code grant with PKCE support.
 *
* @see <a href="https://tools.ietf.org/html/rfc6749#section-4.1">RFC 6749 - Authorization Code Grant</a>
 */
@Data
public class AuthCodeTokenRequest {
    /**
     * Grant type (defaults to "authorization_code").
     * <br/>
     * required
     *
     * @see GrantTypeEnum
     */
    private String grantType = GrantTypeEnum.AUTH_CODE.getValue();
    /**
     * Authorization code received from the authorization server.
     * <br/>
     * required
     */
    private String authCode;
    /**
     * Redirect URI used in the authorization request.
     * <br/>
     * required
     */
    private String redirectUri;
    /**
     * PKCE code verifier.
     * <br/>
     * required when using PKCE
     */
    private String codeVerifier;
    /**
     * Extension parameters for custom Keycloak SPI integrations.
     * <br/>
     * optional - will be flattened into form parameters
     */
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
