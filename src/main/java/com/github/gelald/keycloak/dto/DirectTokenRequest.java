package com.github.gelald.keycloak.dto;

import com.github.gelald.keycloak.enums.GrantTypeEnum;
import lombok.Data;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;

/**
 * Request for Resource Owner Password Credentials grant.
 *
* @see <a href="https://tools.ietf.org/html/rfc6749#section-4.3">RFC 6749 - Resource Owner Password Credentials Grant</a>
 */
@Data
public class DirectTokenRequest {
    /**
     * Grant type (defaults to "password").
     * <br/>
     * required
     *
     * @see GrantTypeEnum
     */
    private String grantType = GrantTypeEnum.PASSWORD.getValue();
    /**
     * Resource owner username.
     * <br/>
     * required
     */
    private String username;
    /**
     * Resource owner password.
     * <br/>
     * required
     */
    private String password;
    /**
     * Requested scope.
     * <br/>
     * optional
     */
    private String scope;
    /**
     * Extension parameters for custom Keycloak SPI integrations.
     * <br/>
     * optional - will be flattened into form parameters
     */
    private Map<String, String> extParams;

    public MultiValueMap<String, String> toMultiValueMap() {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", grantType);
        if (username != null) map.add("username", username);
        if (password != null) map.add("password", password);
        if (scope != null) map.add("scope", scope);
        if (extParams != null) extParams.forEach(map::add);
        return map;
    }
}
