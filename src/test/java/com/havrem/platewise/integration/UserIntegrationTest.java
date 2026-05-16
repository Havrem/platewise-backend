package com.havrem.platewise.integration;

import com.havrem.platewise.dto.auth.LoginRequest;
import com.havrem.platewise.dto.category.CreateCategoryRequest;
import com.havrem.platewise.dto.user.ChangePasswordRequest;
import com.havrem.platewise.entity.Category;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class UserIntegrationTest extends IntegrationTestBase {

    @Test
    void getProfile_returnsCurrentUserInfo() {
        String email = uniqueEmail();
        String token = signupAndGetToken(email);

        client.get().uri("/users/me")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.email").isEqualTo(email)
                .jsonPath("$.id").isNotEmpty();
    }

    @Test
    void changePassword_validRequest_allowsLoginWithNewPassword() {
        String email = uniqueEmail();
        String token = signupAndGetToken(email);

        client.patch().uri("/users/me/password")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ChangePasswordRequest("password123", "newpassword456"))
                .exchange()
                .expectStatus().isNoContent();

        client.post().uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new LoginRequest(email, "newpassword456"))
                .exchange()
                .expectStatus().isOk();

        client.post().uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new LoginRequest(email, "password123"))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void changePassword_wrongCurrentPassword_returns401() {
        String token = signupAndGetToken(uniqueEmail());

        client.patch().uri("/users/me/password")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ChangePasswordRequest("wrong-current-password", "newpassword456"))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void deleteAccount_removesUserAndCascadesData() {
        String email = uniqueEmail();
        String token = signupAndGetToken(email);

        client.post().uri("/categories")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateCategoryRequest("Groceries", "icon", Category.Type.GROCERY))
                .exchange()
                .expectStatus().isCreated();

        client.delete().uri("/users/me")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNoContent();

        // Token now references a deleted user — protected endpoints reject as unauthorized
        client.get().uri("/users/me")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isUnauthorized();

        // Login with old credentials fails
        client.post().uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new LoginRequest(email, "password123"))
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
