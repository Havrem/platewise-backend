package com.havrem.platewise.integration;

import com.havrem.platewise.config.JwtProperties;
import com.havrem.platewise.dto.auth.AuthResponse;
import com.havrem.platewise.dto.auth.LoginRequest;
import com.havrem.platewise.dto.auth.LogoutRequest;
import com.havrem.platewise.dto.auth.RefreshRequest;
import com.havrem.platewise.dto.auth.SignupRequest;
import com.havrem.platewise.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class AuthIntegrationTest extends IntegrationTestBase {
    @Autowired
    JwtProperties jwtProperties;

    @Test
    void signup_validRequest_returns201WithTokensAndPersists() {
        String email = uniqueEmail();

        client.post().uri("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new SignupRequest(email, "password123"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.accessToken").isNotEmpty()
                .jsonPath("$.refreshToken").isNotEmpty()
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
    void login_validCredentials_returns200WithTokens() {
        String email = uniqueEmail();
        signupAndGetToken(email);

        client.post().uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new LoginRequest(email, "password123"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.accessToken").isNotEmpty()
                .jsonPath("$.refreshToken").isNotEmpty()
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
    void protectedEndpoint_withoutToken_returns401() {
        client.get().uri("/categories")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void protectedEndpoint_withGarbageToken_returns401() {
        client.get().uri("/categories")
                .header("Authorization", "Bearer garbage-token-data")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void protectedEndpoint_withExpiredToken_returns401() {
        JwtService expiredTokenIssuer = new JwtService(new JwtProperties(jwtProperties.secret(), -1, jwtProperties.refreshTtlSeconds()));
        String expiredToken = expiredTokenIssuer.generate(1L);

        client.get().uri("/categories")
                .header("Authorization", "Bearer " + expiredToken)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void refresh_validToken_returnsNewPair() {
        AuthResponse signup = signupAndGetAuth(uniqueEmail());

        AuthResponse refreshed = client.post().uri("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new RefreshRequest(signup.refreshToken()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthResponse.class)
                .returnResult()
                .getResponseBody();

        org.assertj.core.api.Assertions.assertThat(refreshed).isNotNull();
        org.assertj.core.api.Assertions.assertThat(refreshed.accessToken()).isNotEmpty();
        org.assertj.core.api.Assertions.assertThat(refreshed.refreshToken()).isNotEmpty();
        org.assertj.core.api.Assertions.assertThat(refreshed.refreshToken()).isNotEqualTo(signup.refreshToken());
    }

    @Test
    void refresh_rotatedToken_invalidatesPrevious() {
        AuthResponse signup = signupAndGetAuth(uniqueEmail());

        client.post().uri("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new RefreshRequest(signup.refreshToken()))
                .exchange()
                .expectStatus().isOk();

        // Reusing the original refresh token after rotation must fail
        client.post().uri("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new RefreshRequest(signup.refreshToken()))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void refresh_invalidToken_returns401() {
        client.post().uri("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new RefreshRequest("not-a-real-token"))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void logout_invalidatesRefreshToken() {
        AuthResponse signup = signupAndGetAuth(uniqueEmail());

        client.post().uri("/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new LogoutRequest(signup.refreshToken()))
                .exchange()
                .expectStatus().isNoContent();

        client.post().uri("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new RefreshRequest(signup.refreshToken()))
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
