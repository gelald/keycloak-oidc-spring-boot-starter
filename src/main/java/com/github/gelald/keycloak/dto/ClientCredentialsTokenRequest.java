package com.github.gelald.keycloak.dto;

import com.github.gelald.keycloak.enums.GrantTypeEnum;
import lombok.Data;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;

/**
 * Request for Client Credentials grant (machine-to-machine).
 *
* @see <a href="https://tools.ietf.org/html/rfc6749#section-4.4">RFC 6749 - Client Credentials Grant</a>
 */
@Data
public class ClientCredentialsTokenRequest {
    /**
     * Grant type (defaults to "client_credentials").
     * <br/>
     * required
     *
     * @see GrantTypeEnum
     */
    private String grantType = GrantTypeEnum.CLIENT.getValue();
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
        if (scope != null) map.add("scope", scope);
        if (extParams != null) extParams.forEach(map::add);
        return map;
    }
}
