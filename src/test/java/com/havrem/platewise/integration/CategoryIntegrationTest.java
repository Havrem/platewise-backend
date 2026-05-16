package com.havrem.platewise.integration;

import com.havrem.platewise.dto.category.CategoryDto;
import com.havrem.platewise.dto.category.CreateCategoryRequest;
import com.havrem.platewise.dto.category.UpdateCategoryRequest;
import com.havrem.platewise.entity.Category;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryIntegrationTest extends IntegrationTestBase {

    @Test
    void fullCrudFlow_persistsAndReturnsCategory() {
        String token = signupAndGetToken(uniqueEmail());

        CategoryDto created = client.post().uri("/categories")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateCategoryRequest("Groceries", "shopping-cart", Category.Type.GROCERY))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CategoryDto.class)
                .returnResult()
                .getResponseBody();

        assertThat(created).isNotNull();
        Long id = created.id();

        client.get().uri("/categories/" + id)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(id)
                .jsonPath("$.name").isEqualTo("Groceries")
                .jsonPath("$.type").isEqualTo("GROCERY");

        client.get().uri("/categories")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1);

        client.put().uri("/categories/" + id)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new UpdateCategoryRequest("Renamed", "icon", Category.Type.RECIPES))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Renamed")
                .jsonPath("$.type").isEqualTo("RECIPES");

        client.delete().uri("/categories/" + id)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNoContent();

        client.get().uri("/categories/" + id)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void userB_cannotAccessOrModifyUserAsCategory() {
        String tokenA = signupAndGetToken(uniqueEmail());
        String tokenB = signupAndGetToken(uniqueEmail());

        CategoryDto createdA = client.post().uri("/categories")
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateCategoryRequest("A's groceries", "icon", Category.Type.GROCERY))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CategoryDto.class)
                .returnResult()
                .getResponseBody();

        assertThat(createdA).isNotNull();
        Long idA = createdA.id();

        client.get().uri("/categories/" + idA)
                .header("Authorization", "Bearer " + tokenB)
                .exchange()
                .expectStatus().isNotFound();

        client.put().uri("/categories/" + idA)
                .header("Authorization", "Bearer " + tokenB)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new UpdateCategoryRequest("hijacked", "icon", Category.Type.GROCERY))
                .exchange()
                .expectStatus().isNotFound();

        client.delete().uri("/categories/" + idA)
                .header("Authorization", "Bearer " + tokenB)
                .exchange()
                .expectStatus().isNotFound();

        client.get().uri("/categories")
                .header("Authorization", "Bearer " + tokenB)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(0);
    }
}
