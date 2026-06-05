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
    /**
     * Whether the token is currently active.
     */
    private Boolean active;
    /**
     * Token type (e.g. "Bearer").
     */
    @JsonProperty("typ")
    private String type;
    /**
     * Token expiration timestamp (seconds since epoch).
     */
    @JsonProperty("exp")
    private Long expire;
    /**
     * Subject - identifier for the end-user.
     */
    @JsonProperty("sub")
    private String subject;
    /**
     * Username of the token holder.
     */
    private String username;
    /**
     * Audience - the intended recipient(s) of the token.
     */
    @JsonProperty("aud")
    private String audience;
    /**
     * Issuer - the party that issued the token.
     */
    @JsonProperty("iss")
    private String issuer;
    /**
     * Issued at timestamp (seconds since epoch).
     */
    @JsonProperty("iat")
    private Long issuedAt;
    /**
     * Scope of the token.
     */
    private String scope;
    /**
     * Client ID that requested the token.
     */
    @JsonProperty("client_id")
    private String clientId;
    /**
     * Realm-level roles (Keycloak-specific).
     */
    @JsonProperty("realm_access")
    private Map<String, List<String>> realmAccess;
    /**
     * Resource-level roles (Keycloak-specific).
     */
    @JsonProperty("resource_access")
    private Map<String, Map<String, List<String>>> resourceAccess;
}
