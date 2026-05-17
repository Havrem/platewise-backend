package com.havrem.platewise.integration;

import com.havrem.platewise.dto.category.CategoryDto;
import com.havrem.platewise.dto.category.CreateCategoryRequest;
import com.havrem.platewise.dto.item.CreateItemRequest;
import com.havrem.platewise.dto.item.ItemDto;
import com.havrem.platewise.dto.item.ReorderItemRequest;
import com.havrem.platewise.dto.item.UpdateItemRequest;
import com.havrem.platewise.dto.itemList.CreateItemListRequest;
import com.havrem.platewise.dto.itemList.ItemListDto;
import com.havrem.platewise.entity.Item;
import com.havrem.platewise.entity.ItemList;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

class ItemIntegrationTest extends IntegrationTestBase {

    @Test
    void fullCrudFlow_persistsAndReturnsItem() {
        String token = signupAndGetToken(uniqueEmail());
        Long categoryId = createCategory(token, "Groceries");
        Long itemListId = createItemList(token, "Weekly", categoryId);

        ItemDto created = client.post().uri("/items")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateItemRequest("Milk", false, Item.Type.CHECKED, itemListId))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ItemDto.class)
                .returnResult()
                .getResponseBody();

        assertThat(created).isNotNull();
        Long id = created.id();

        client.get().uri("/items/" + id)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(id)
                .jsonPath("$.text").isEqualTo("Milk")
                .jsonPath("$.completed").isEqualTo(false)
                .jsonPath("$.type").isEqualTo("CHECKED")
                .jsonPath("$.itemListId").isEqualTo(itemListId);

        client.get().uri("/items")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1);

        client.patch().uri("/items/" + id)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new UpdateItemRequest("Whole milk", true, Item.Type.CHECKED))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.text").isEqualTo("Whole milk")
                .jsonPath("$.completed").isEqualTo(true);

        client.delete().uri("/items/" + id)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNoContent();

        client.get().uri("/items/" + id)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void userB_cannotAccessOrModifyUserAsItem() {
        String tokenA = signupAndGetToken(uniqueEmail());
        String tokenB = signupAndGetToken(uniqueEmail());

        Long categoryIdA = createCategory(tokenA, "A's groceries");
        Long itemListIdA = createItemList(tokenA, "A's list", categoryIdA);
        Long itemIdA = createItem(tokenA, "A's milk", itemListIdA);

        client.get().uri("/items/" + itemIdA)
                .header("Authorization", "Bearer " + tokenB)
                .exchange()
                .expectStatus().isNotFound();

        client.patch().uri("/items/" + itemIdA)
                .header("Authorization", "Bearer " + tokenB)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new UpdateItemRequest("hijacked", true, Item.Type.CHECKED))
                .exchange()
                .expectStatus().isNotFound();

        client.delete().uri("/items/" + itemIdA)
                .header("Authorization", "Bearer " + tokenB)
                .exchange()
                .expectStatus().isNotFound();

        // user B trying to create an item under user A's itemlist is also rejected
        client.post().uri("/items")
                .header("Authorization", "Bearer " + tokenB)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateItemRequest("trying-to-steal-list", false, Item.Type.CHECKED, itemListIdA))
                .exchange()
                .expectStatus().isNotFound();

        client.get().uri("/items")
                .header("Authorization", "Bearer " + tokenB)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    void create_appendsToEndOfItemListByRank() {
        String token = signupAndGetToken(uniqueEmail());
        Long categoryId = createCategory(token, "Groceries");
        Long itemListId = createItemList(token, "Weekly", categoryId);

        Long firstId = createItem(token, "First", itemListId);
        Long secondId = createItem(token, "Second", itemListId);
        Long thirdId = createItem(token, "Third", itemListId);

        client.get().uri("/items")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(firstId)
                .jsonPath("$[1].id").isEqualTo(secondId)
                .jsonPath("$[2].id").isEqualTo(thirdId);
    }

    @Test
    void reorder_movesItemBetweenNeighbors() {
        String token = signupAndGetToken(uniqueEmail());
        Long categoryId = createCategory(token, "Groceries");
        Long itemListId = createItemList(token, "Weekly", categoryId);
        Long firstId = createItem(token, "First", itemListId);
        Long secondId = createItem(token, "Second", itemListId);
        Long thirdId = createItem(token, "Third", itemListId);

        client.patch().uri("/items/" + firstId + "/order")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ReorderItemRequest(secondId, thirdId))
                .exchange()
                .expectStatus().isOk();

        client.get().uri("/items")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(secondId)
                .jsonPath("$[1].id").isEqualTo(firstId)
                .jsonPath("$[2].id").isEqualTo(thirdId);
    }

    @Test
    void reorder_neighborInDifferentItemList_returns400() {
        String token = signupAndGetToken(uniqueEmail());
        Long categoryId = createCategory(token, "Groceries");
        Long listA = createItemList(token, "List A", categoryId);
        Long listB = createItemList(token, "List B", categoryId);
        Long itemInA = createItem(token, "In A", listA);
        Long itemInB = createItem(token, "In B", listB);

        client.patch().uri("/items/" + itemInA + "/order")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ReorderItemRequest(itemInB, null))
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

    private Long createItem(String token, String text, Long itemListId) {
        ItemDto created = client.post().uri("/items")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateItemRequest(text, false, Item.Type.CHECKED, itemListId))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ItemDto.class)
                .returnResult()
                .getResponseBody();
        assertThat(created).isNotNull();
        return created.id();
    }
}
