package com.havrem.platewise.integration;

import com.havrem.platewise.dto.category.CategoryDto;
import com.havrem.platewise.dto.category.CreateCategoryRequest;
import com.havrem.platewise.dto.itemList.CreateItemListRequest;
import com.havrem.platewise.dto.itemList.ItemListDto;
import com.havrem.platewise.dto.itemList.ReorderItemListRequest;
import com.havrem.platewise.dto.itemList.UpdateItemListRequest;
import com.havrem.platewise.entity.ItemList;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

class ItemListIntegrationTest extends IntegrationTestBase {

    @Test
    void fullCrudFlow_persistsAndReturnsItemList() {
        String token = signupAndGetToken(uniqueEmail());
        Long categoryId = createCategory(token, "Groceries");

        ItemListDto created = client.post().uri("/item-lists")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateItemListRequest("Weekly", categoryId, ItemList.Type.GROCERY))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ItemListDto.class)
                .returnResult()
                .getResponseBody();

        assertThat(created).isNotNull();
        Long id = created.id();

        client.get().uri("/item-lists/" + id)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(id)
                .jsonPath("$.title").isEqualTo("Weekly")
                .jsonPath("$.category.id").isEqualTo(categoryId)
                .jsonPath("$.type").isEqualTo("GROCERY")
                .jsonPath("$.bookmarked").isEqualTo(false);

        client.get().uri("/item-lists")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1);

        client.patch().uri("/item-lists/" + id)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new UpdateItemListRequest("Renamed", categoryId, ItemList.Type.RECIPES, true))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Renamed")
                .jsonPath("$.type").isEqualTo("RECIPES")
                .jsonPath("$.bookmarked").isEqualTo(true);

        client.delete().uri("/item-lists/" + id)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNoContent();

        client.get().uri("/item-lists/" + id)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void userB_cannotAccessOrModifyUserAsItemList() {
        String tokenA = signupAndGetToken(uniqueEmail());
        String tokenB = signupAndGetToken(uniqueEmail());

        Long categoryIdA = createCategory(tokenA, "A's groceries");
        Long itemListIdA = createItemList(tokenA, "A's weekly list", categoryIdA);
        Long categoryIdB = createCategory(tokenB, "B's groceries");

        client.get().uri("/item-lists/" + itemListIdA)
                .header("Authorization", "Bearer " + tokenB)
                .exchange()
                .expectStatus().isNotFound();

        client.patch().uri("/item-lists/" + itemListIdA)
                .header("Authorization", "Bearer " + tokenB)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new UpdateItemListRequest("hijacked", categoryIdB, ItemList.Type.GENERAL, true))
                .exchange()
                .expectStatus().isNotFound();

        client.delete().uri("/item-lists/" + itemListIdA)
                .header("Authorization", "Bearer " + tokenB)
                .exchange()
                .expectStatus().isNotFound();

        // user B trying to create an itemlist using user A's category is also rejected
        client.post().uri("/item-lists")
                .header("Authorization", "Bearer " + tokenB)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateItemListRequest("trying-to-steal-category", categoryIdA, ItemList.Type.GROCERY))
                .exchange()
                .expectStatus().isNotFound();

        client.get().uri("/item-lists")
                .header("Authorization", "Bearer " + tokenB)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    void create_appendsToEndOfCategoryByRank() {
        String token = signupAndGetToken(uniqueEmail());
        Long categoryId = createCategory(token, "Groceries");

        Long firstId = createItemList(token, "First", categoryId);
        Long secondId = createItemList(token, "Second", categoryId);
        Long thirdId = createItemList(token, "Third", categoryId);

        client.get().uri("/item-lists")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(firstId)
                .jsonPath("$[1].id").isEqualTo(secondId)
                .jsonPath("$[2].id").isEqualTo(thirdId);
    }

    @Test
    void reorder_movesListBetweenNeighbors() {
        String token = signupAndGetToken(uniqueEmail());
        Long categoryId = createCategory(token, "Groceries");
        Long firstId = createItemList(token, "First", categoryId);
        Long secondId = createItemList(token, "Second", categoryId);
        Long thirdId = createItemList(token, "Third", categoryId);

        // Move first to between second and third
        client.patch().uri("/item-lists/" + firstId + "/order")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ReorderItemListRequest(secondId, thirdId))
                .exchange()
                .expectStatus().isOk();

        client.get().uri("/item-lists")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(secondId)
                .jsonPath("$[1].id").isEqualTo(firstId)
                .jsonPath("$[2].id").isEqualTo(thirdId);
    }

    @Test
    void reorder_movesListToStart() {
        String token = signupAndGetToken(uniqueEmail());
        Long categoryId = createCategory(token, "Groceries");
        Long firstId = createItemList(token, "First", categoryId);
        Long secondId = createItemList(token, "Second", categoryId);
        Long thirdId = createItemList(token, "Third", categoryId);

        client.patch().uri("/item-lists/" + thirdId + "/order")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ReorderItemListRequest(null, firstId))
                .exchange()
                .expectStatus().isOk();

        client.get().uri("/item-lists")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(thirdId)
                .jsonPath("$[1].id").isEqualTo(firstId)
                .jsonPath("$[2].id").isEqualTo(secondId);
    }

    @Test
    void reorder_neighborInDifferentCategory_returns400() {
        String token = signupAndGetToken(uniqueEmail());
        Long catA = createCategory(token, "A");
        Long catB = createCategory(token, "B");
        Long inA = createItemList(token, "In A", catA);
        Long inB = createItemList(token, "In B", catB);

        client.patch().uri("/item-lists/" + inA + "/order")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ReorderItemListRequest(inB, null))
                .exchange()
                .expectStatus().isBadRequest();
    }

    private Long createCategory(String token, String name) {
        CategoryDto created = client.post().uri("/categories")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateCategoryRequest(name, "icon"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CategoryDto.class)
                .returnResult()
                .getResponseBody();
        assertThat(created).isNotNull();
        return created.id();
    }

    private Long createItemList(String token, String title, Long categoryId) {
        ItemListDto created = client.post().uri("/item-lists")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateItemListRequest(title, categoryId, ItemList.Type.GROCERY))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ItemListDto.class)
                .returnResult()
                .getResponseBody();
        assertThat(created).isNotNull();
        return created.id();
    }
}
