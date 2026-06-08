package com.github.gelald.keycloak.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * JWKS certificate response containing public keys for JWT signature verification.
 *
 * @see <a href="https://tools.ietf.org/html/rfc7517">RFC 7517 - JSON Web Key (JWK)</a>
 */
@Data
public class CertificateResponse {
    /** RFC 7517 parameter: {@code keys}. The list of JWK key objects in the key set. */
    private List<CertificateData> keys;

    /**
     * A JSON Web Key (JWK) representing a single public key.
     */
    @Data
    public static class CertificateData {
        /** RFC 7517 parameter: {@code kid}. Key ID — unique identifier for the key. */
        @JsonProperty("kid")
        private String keyId;
        /** RFC 7517 parameter: {@code kty}. Key Type (e.g. "RSA"). */
        @JsonProperty("kty")
        private String keyType;
        /** RFC 7517 parameter: {@code alg}. Algorithm intended for use with the key (e.g. "RS256"). */
        @JsonProperty("alg")
        private String algorithm;
        /** RFC 7517 parameter: {@code use}. Public key use (e.g. "sig" for signature, "enc" for encryption). */
        private String use;
        /** RFC 7517 parameter: {@code n}. Base64url-encoded RSA modulus. */
        @JsonProperty("n")
        private String modulus;
        /** RFC 7517 parameter: {@code e}. Base64url-encoded RSA exponent. */
        @JsonProperty("e")
        private String exponent;
    }
}
