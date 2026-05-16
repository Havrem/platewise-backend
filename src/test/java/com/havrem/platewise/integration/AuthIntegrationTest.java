package com.havrem.platewise.integration;

import com.havrem.platewise.config.JwtProperties;
import com.havrem.platewise.dto.auth.LoginRequest;
import com.havrem.platewise.dto.auth.SignupRequest;
import com.havrem.platewise.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class AuthIntegrationTest extends IntegrationTestBase {
    @Autowired
    JwtProperties jwtProperties;

    @Test
    void signup_validRequest_returns201WithTokenAndPersists() {
        String email = uniqueEmail();

        client.post().uri("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new SignupRequest(email, "password123"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.accessToken").isNotEmpty()
                .jsonPath("$.email").isEqualTo(email);

        client.post().uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new LoginRequest(email, "password123"))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void signup_duplicateEmail_returns409() {
        String email = uniqueEmail();

        client.post().uri("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new SignupRequest(email, "password123"))
                .exchange()
                .expectStatus().isCreated();

        client.post().uri("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new SignupRequest(email, "password123"))
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    void signup_invalidEmail_returns400() {
        client.post().uri("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new SignupRequest("not-an-email", "password123"))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void login_validCredentials_returns200WithToken() {
        String email = uniqueEmail();
        signupAndGetToken(email);

        client.post().uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new LoginRequest(email, "password123"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.accessToken").isNotEmpty()
                .jsonPath("$.email").isEqualTo(email);
    }

    @Test
    void login_wrongPassword_returns401() {
        String email = uniqueEmail();
        signupAndGetToken(email);

        client.post().uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new LoginRequest(email, "wrong-password"))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void login_unknownEmail_returns401() {
        client.post().uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new LoginRequest(uniqueEmail(), "password123"))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void protectedEndpoint_withoutToken_returns403() {
        client.get().uri("/categories")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void protectedEndpoint_withGarbageToken_returns403() {
        client.get().uri("/categories")
                .header("Authorization", "Bearer garbage-token-data")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void protectedEndpoint_withExpiredToken_returns403() {
        JwtService expiredTokenIssuer = new JwtService(new JwtProperties(jwtProperties.secret(), -1));
        String expiredToken = expiredTokenIssuer.generate(1L);

        client.get().uri("/categories")
                .header("Authorization", "Bearer " + expiredToken)
                .exchange()
                .expectStatus().isForbidden();
    }
}
