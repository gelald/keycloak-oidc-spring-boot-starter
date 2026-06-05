package com.github.gelald.keycloak.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PkceUtilsTest {

    @Test
    @DisplayName("generateCodeVerifier - returns a non-blank string")
    void generateCodeVerifier_nonBlank() {
        String verifier = PkceUtils.generateCodeVerifier();
        assertThat(verifier).isNotBlank();
    }

    @Test
    @DisplayName("generateCodeVerifier - returns URL-safe base64 string")
    void generateCodeVerifier_urlSafe() {
        String verifier = PkceUtils.generateCodeVerifier();
        assertThat(verifier).doesNotContain("+", "/", "=");
    }

    @Test
    @DisplayName("generateCodeVerifier - length is between 43 and 128 characters")
    void generateCodeVerifier_length() {
        String verifier = PkceUtils.generateCodeVerifier();
        assertThat(verifier).hasSizeBetween(43, 128);
    }

    @RepeatedTest(10)
    @DisplayName("generateCodeVerifier - produces different values each call")
    void generateCodeVerifier_randomness() {
        assertThat(PkceUtils.generateCodeVerifier())
                .isNotEqualTo(PkceUtils.generateCodeVerifier());
    }

    @Test
    @DisplayName("generateCodeChallengeS256 - produces consistent output for same input")
    void generateCodeChallengeS256_consistent() {
        String verifier = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk";
        String challenge1 = PkceUtils.generateCodeChallengeS256(verifier);
        String challenge2 = PkceUtils.generateCodeChallengeS256(verifier);
        assertThat(challenge1).isEqualTo(challenge2);
    }

    @Test
    @DisplayName("generateCodeChallengeS256 - output matches known RFC 7636 test vector")
    void generateCodeChallengeS256_knownVector() {
        String verifier = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk";
        // Expected: Base64URL-encoded SHA-256 of verifier
        String challenge = PkceUtils.generateCodeChallengeS256(verifier);
        assertThat(challenge).isEqualTo("E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM");
    }

    @Test
    @DisplayName("generateCodeChallengeS256 - produces URL-safe base64 string (no padding)")
    void generateCodeChallengeS256_urlSafe() {
        String challenge = PkceUtils.generateCodeChallengeS256(PkceUtils.generateCodeVerifier());
        assertThat(challenge).doesNotContain("+", "/", "=");
    }
}
