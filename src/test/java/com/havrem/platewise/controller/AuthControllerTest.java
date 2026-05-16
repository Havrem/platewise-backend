package com.havrem.platewise.controller;

import com.havrem.platewise.dto.auth.AuthResponse;
import com.havrem.platewise.dto.auth.LoginRequest;
import com.havrem.platewise.dto.auth.SignupRequest;
import com.havrem.platewise.exception.ConflictException;
import com.havrem.platewise.exception.GlobalExceptionHandler;
import com.havrem.platewise.exception.UnauthorizedException;
import com.havrem.platewise.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebMvcTest(AuthController.class)
@AutoConfigureRestTestClient
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
public class AuthControllerTest {
    @Autowired
    RestTestClient client;

    @MockitoBean
    AuthService authService;

    @Test
    void signup_validRequest_returns201WithToken() {
        when(authService.signup(any()))
                .thenReturn(new AuthResponse("token-abc", 1L, "user@example.com"));

        client.post().uri("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new SignupRequest("user@example.com", "password123"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.accessToken").isEqualTo("token-abc")
                .jsonPath("$.email").isEqualTo("user@example.com");
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
    void signup_duplicateEmail_returns409() {
        when(authService.signup(any()))
                .thenThrow(new ConflictException("Email already in use."));

        client.post().uri("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new SignupRequest("user@example.com", "password123"))
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    void login_validCredentials_returns200WithToken() {
        when(authService.login(any()))
                .thenReturn(new AuthResponse("token-xyz", 1L, "user@example.com"));

        client.post().uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new LoginRequest("user@example.com", "password123"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.accessToken").isEqualTo("token-xyz")
                .jsonPath("$.email").isEqualTo("user@example.com");
    }

    @Test
    void login_invalidCredentials_returns401() {
        when(authService.login(any()))
                .thenThrow(new UnauthorizedException("Invalid email or password."));

        client.post().uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new LoginRequest("user@example.com", "wrong"))
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
