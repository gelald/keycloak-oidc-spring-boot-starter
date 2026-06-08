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
 * Request for refreshing an access token.
 *
 * @see <a href="https://tools.ietf.org/html/rfc6749#section-6">RFC 6749 - Refreshing an Access Token</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequest {
    /** OAuth2 parameter: {@code grant_type}. Defaults to "refresh_token". Required. */
    @Builder.Default
    private String grantType = GrantTypeEnum.REFRESH.getValue();
    /** OAuth2 parameter: {@code refresh_token}. The refresh token issued to the client. Required. */
    private String refreshToken;
    /** Extension parameters for custom Keycloak SPI integrations. Optional — will be flattened into form parameters. */
    private Map<String, String> extParams;

    public MultiValueMap<String, String> toMultiValueMap() {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", grantType);
        if (refreshToken != null) map.add("refresh_token", refreshToken);
        if (extParams != null) extParams.forEach(map::add);
        return map;
    }
}
