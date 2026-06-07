package com.github.gelald.keycloak.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Token introspection response (RFC 7662).
 *
* @see <a href="https://tools.ietf.org/html/rfc7662">RFC 7662 - OAuth 2.0 Token Introspection</a>
 */
@Data
public class IntrospectResponse {
    /** RFC 7662 parameter: {@code active}. Indicates whether the token is currently active. */
    private Boolean active;
    /** RFC 7662 parameter: {@code typ}. The token type (e.g. "Bearer"). */
    @JsonProperty("typ")
    private String type;
    /** RFC 7519 claim: {@code exp}. Token expiration timestamp (seconds since epoch). */
    @JsonProperty("exp")
    private Long expire;
    /** RFC 7519 claim: {@code sub}. Subject identifier for the end-user. */
    @JsonProperty("sub")
    private String subject;
    /** RFC 7662 parameter: {@code username}. Human-readable identifier for the token holder. */
    private String username;
    /** RFC 7519 claim: {@code aud}. The intended recipient(s) of the token. */
    @JsonProperty("aud")
    private String audience;
    /** RFC 7519 claim: {@code iss}. The party that issued the token. */
    @JsonProperty("iss")
    private String issuer;
    /** RFC 7519 claim: {@code iat}. Issued-at timestamp (seconds since epoch). */
    @JsonProperty("iat")
    private Long issuedAt;
    /** RFC 7662 parameter: {@code scope}. The scope of the token. */
    private String scope;
    /** RFC 7662 parameter: {@code client_id}. The client identifier that requested the token. */
    @JsonProperty("client_id")
    private String clientId;
    /** Keycloak-specific parameter: {@code realm_access}. Realm-level role mappings. */
    @JsonProperty("realm_access")
    private Map<String, List<String>> realmAccess;
    /** Keycloak-specific parameter: {@code resource_access}. Resource-level role mappings. */
    @JsonProperty("resource_access")
    private Map<String, Map<String, List<String>>> resourceAccess;
}
