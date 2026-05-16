package com.havrem.platewise.service;

import com.havrem.platewise.config.JwtProperties;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "test-secret-that-is-long-enough-to-meet-hmac-sha-key-requirements-for-jjwt-library";

    @Test
    void extractUserId_expiredToken_throwsExpiredJwtException() {
        JwtService jwtService = new JwtService(new JwtProperties(SECRET, -1));

        String expiredToken = jwtService.generate(42L);

        assertThatThrownBy(() -> jwtService.extractUserId(expiredToken))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void extractUserId_tokenSignedByDifferentSecret_throwsSignatureException() {
        JwtService issuer = new JwtService(new JwtProperties(SECRET, 900));
        JwtService verifier = new JwtService(new JwtProperties("different-secret-also-long-enough-to-meet-hmac-sha-key-requirements", 900));

        String token = issuer.generate(42L);

        assertThatThrownBy(() -> verifier.extractUserId(token))
                .isInstanceOf(SignatureException.class);
    }
}
