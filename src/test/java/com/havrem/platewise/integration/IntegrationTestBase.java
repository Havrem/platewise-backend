package com.havrem.platewise.integration;

import com.havrem.platewise.TestcontainersConfiguration;
import com.havrem.platewise.dto.auth.AuthResponse;
import com.havrem.platewise.dto.auth.SignupRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@Import(TestcontainersConfiguration.class)
public abstract class IntegrationTestBase {

    @Autowired
    protected RestTestClient client;

    protected String uniqueEmail() {
        return "test-" + UUID.randomUUID() + "@example.com";
    }

    protected String signupAndGetToken(String email) {
        AuthResponse response = client.post().uri("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new SignupRequest(email, "password123"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(AuthResponse.class)
                .returnResult()
                .getResponseBody();
        return response.accessToken();
    }
}
