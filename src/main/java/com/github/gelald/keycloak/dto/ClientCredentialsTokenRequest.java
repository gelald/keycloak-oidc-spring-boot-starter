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
 * Request for Client Credentials grant (machine-to-machine).
 *
* @see <a href="https://tools.ietf.org/html/rfc6749#section-4.4">RFC 6749 - Client Credentials Grant</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientCredentialsTokenRequest {
    /** OAuth2 parameter: {@code grant_type}. Defaults to "client_credentials". Required. */
    @Builder.Default
    private String grantType = GrantTypeEnum.CLIENT.getValue();
    /** OAuth2 parameter: {@code scope}. The requested scope for the access token. Optional. */
    private String scope;
    /** Extension parameters for custom Keycloak SPI integrations. Optional — will be flattened into form parameters. */
    private Map<String, String> extParams;

    public MultiValueMap<String, String> toMultiValueMap() {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", grantType);
        if (scope != null) map.add("scope", scope);
        if (extParams != null) extParams.forEach(map::add);
        return map;
    }
}
