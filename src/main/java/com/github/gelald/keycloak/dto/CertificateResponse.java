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
    /**
     * List of JWK key objects.
     */
    private List<CertificateData> keys;

    @Data
    private static class CertificateData {
        /**
         * Key ID - unique identifier for the key.
         */
        @JsonProperty("kid")
        private String keyId;
        /**
         * Key Type (e.g. "RSA").
         */
        @JsonProperty("kty")
        private String keyType;
        /**
         * Algorithm (e.g. "RS256").
         */
        @JsonProperty("alg")
        private String algorithm;
        /**
         * Public key use (e.g. "sig" for signature, "enc" for encryption).
         */
        private String use;
        /**
         * RSA modulus.
         */
        @JsonProperty("n")
        private String modulus;
        /**
         * RSA exponent.
         */
        @JsonProperty("e")
        private String exponent;
    }
}
