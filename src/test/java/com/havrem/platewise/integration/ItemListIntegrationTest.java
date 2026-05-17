package com.havrem.platewise.integration;

import com.havrem.platewise.dto.category.CategoryDto;
import com.havrem.platewise.dto.category.CreateCategoryRequest;
import com.havrem.platewise.dto.item.CreateItemRequest;
import com.havrem.platewise.dto.item.ItemDto;
import com.havrem.platewise.dto.item.ReorderItemRequest;
import com.havrem.platewise.dto.itemList.CreateItemListRequest;
import com.havrem.platewise.dto.itemList.ImportItemsRequest;
import com.havrem.platewise.dto.itemList.ItemListDto;
import com.havrem.platewise.dto.itemList.ReorderItemListRequest;
import com.havrem.platewise.dto.itemList.UpdateItemListRequest;
import com.havrem.platewise.dto.listSection.CreateListSectionRequest;
import com.havrem.platewise.dto.listSection.ListSectionDto;
import com.havrem.platewise.dto.listSection.ReorderListSectionRequest;
import com.havrem.platewise.dto.listSection.UpdateListSectionRequest;
import com.havrem.platewise.entity.Item;
import com.havrem.platewise.entity.ItemList;
import com.havrem.platewise.service.GroceryOrganization;
import com.havrem.platewise.service.GroceryOrganizer;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ItemListIntegrationTest extends IntegrationTestBase {
    @MockitoBean
    private GroceryOrganizer groceryOrganizer;

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

    @Test
    void importItems_copiesSourceItemsIntoTargetList() {
        String token = signupAndGetToken(uniqueEmail());
        Long categoryId = createCategory(token, "Groceries");
        Long sourceId = createItemList(token, "Template", categoryId);
        Long targetId = createItemList(token, "Weekly", categoryId);

        createItem(token, "Existing", false, Item.Type.NONE, targetId);
        Long milkId = createItem(token, "Milk", true, Item.Type.CHECKED, sourceId);
        Long breadId = createItem(token, "Bread", false, Item.Type.BULLET, sourceId);

        ItemListDto imported = client.post().uri("/item-lists/" + targetId + "/items/import")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ImportItemsRequest(sourceId))
                .exchange()
                .expectStatus().isOk()
                .expectBody(ItemListDto.class)
                .returnResult()
                .getResponseBody();

        assertThat(imported).isNotNull();
        assertThat(imported.items()).extracting(ItemDto::text).containsExactly("Existing", "Milk", "Bread");
        assertThat(imported.items()).extracting(ItemDto::completed).containsExactly(false, true, false);
        assertThat(imported.items()).extracting(ItemDto::type).containsExactly(Item.Type.NONE, Item.Type.CHECKED, Item.Type.BULLET);
        assertThat(imported.items().get(1).id()).isNotEqualTo(milkId);
        assertThat(imported.items().get(2).id()).isNotEqualTo(breadId);

        client.get().uri("/item-lists/" + sourceId)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.items.length()").isEqualTo(2);
    }

    @Test
    void importItems_sameSourceAndTarget_returns400() {
        String token = signupAndGetToken(uniqueEmail());
        Long categoryId = createCategory(token, "Groceries");
        Long listId = createItemList(token, "Weekly", categoryId);

        client.post().uri("/item-lists/" + listId + "/items/import")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ImportItemsRequest(listId))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void importItems_userCannotImportFromInaccessibleSource() {
        String tokenA = signupAndGetToken(uniqueEmail());
        String tokenB = signupAndGetToken(uniqueEmail());
        Long categoryA = createCategory(tokenA, "A");
        Long categoryB = createCategory(tokenB, "B");
        Long sourceIdA = createItemList(tokenA, "A source", categoryA);
        Long targetIdB = createItemList(tokenB, "B target", categoryB);

        client.post().uri("/item-lists/" + targetIdB + "/items/import")
                .header("Authorization", "Bearer " + tokenB)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ImportItemsRequest(sourceIdA))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void sections_canBeCreatedUpdatedReorderedAndAssignedToItems() {
        String token = signupAndGetToken(uniqueEmail());
        Long categoryId = createCategory(token, "Groceries");
        Long listId = createItemList(token, "Weekly", categoryId);

        ListSectionDto dairy = createSection(token, listId, "Dairy");
        ListSectionDto produce = createSection(token, listId, "Produce");

        ItemDto milk = client.post().uri("/items")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateItemRequest("Milk", false, Item.Type.CHECKED, listId, dairy.id()))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ItemDto.class)
                .returnResult()
                .getResponseBody();

        assertThat(milk).isNotNull();
        assertThat(milk.sectionId()).isEqualTo(dairy.id());

        client.patch().uri("/list-sections/" + dairy.id())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new UpdateListSectionRequest("Cold stuff"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.text").isEqualTo("Cold stuff");

        client.patch().uri("/list-sections/" + produce.id() + "/order")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ReorderListSectionRequest(null, dairy.id()))
                .exchange()
                .expectStatus().isOk();

        client.patch().uri("/items/" + milk.id() + "/order")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ReorderItemRequest(null, null, produce.id()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.sectionId").isEqualTo(produce.id());

        client.get().uri("/item-lists/" + listId)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.sections[0].id").isEqualTo(produce.id())
                .jsonPath("$.sections[1].id").isEqualTo(dairy.id())
                .jsonPath("$.items[0].sectionId").isEqualTo(produce.id());

        client.delete().uri("/list-sections/" + produce.id())
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNoContent();

        client.get().uri("/item-lists/" + listId)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.sections.length()").isEqualTo(1)
                .jsonPath("$.items[0].sectionId").doesNotExist();
    }

    @Test
    void organizeGrocerySections_usesGeminiPlanToCreateSectionsAndAssignItems() {
        String token = signupAndGetToken(uniqueEmail());
        Long categoryId = createCategory(token, "Groceries");
        Long listId = createItemList(token, "Weekly", categoryId);

        Long milkId = createItem(token, "Milk", false, Item.Type.NONE, listId);
        Long bananasId = createItem(token, "Bananas", false, Item.Type.NONE, listId);
        Long soapId = createItem(token, "Dish soap", false, Item.Type.NONE, listId);

        when(groceryOrganizer.organize(any(), any(), any())).thenReturn(new GroceryOrganization(List.of(
                new GroceryOrganization.Section("Produce", List.of(bananasId)),
                new GroceryOrganization.Section("Dairy", List.of(milkId)),
                new GroceryOrganization.Section("Other", List.of(soapId))
        )));

        ItemListDto organized = client.post().uri("/item-lists/" + listId + "/sections/organize")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ItemListDto.class)
                .returnResult()
                .getResponseBody();

        assertThat(organized).isNotNull();
        assertThat(organized.sections()).extracting(ListSectionDto::text).containsExactly("Produce", "Dairy", "Other");
        assertThat(organized.items()).extracting(ItemDto::text).containsExactly("Bananas", "Milk", "Dish soap");
        assertThat(organized.items().get(0).sectionId()).isEqualTo(organized.sections().get(0).id());
        assertThat(organized.items().get(1).sectionId()).isEqualTo(organized.sections().get(1).id());
        assertThat(organized.items().get(2).sectionId()).isEqualTo(organized.sections().get(2).id());
    }

    @Test
    void organizeGrocerySections_rejectsNonGroceryList() {
        String token = signupAndGetToken(uniqueEmail());
        Long categoryId = createCategory(token, "Notes");
        ItemListDto list = client.post().uri("/item-lists")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateItemListRequest("Ideas", categoryId, ItemList.Type.GENERAL))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ItemListDto.class)
                .returnResult()
                .getResponseBody();
        assertThat(list).isNotNull();

        client.post().uri("/item-lists/" + list.id() + "/sections/organize")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void organizeGrocerySections_rejectsInvalidGeminiPlan() {
        String token = signupAndGetToken(uniqueEmail());
        Long categoryId = createCategory(token, "Groceries");
        Long listId = createItemList(token, "Weekly", categoryId);

        Long milkId = createItem(token, "Milk", false, Item.Type.NONE, listId);
        createItem(token, "Bananas", false, Item.Type.NONE, listId);

        when(groceryOrganizer.organize(any(), any(), any())).thenReturn(new GroceryOrganization(List.of(
                new GroceryOrganization.Section("Dairy", List.of(milkId))
        )));

        client.post().uri("/item-lists/" + listId + "/sections/organize")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.BAD_GATEWAY);
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

    private ListSectionDto createSection(String token, Long listId, String text) {
        ListSectionDto created = client.post().uri("/item-lists/" + listId + "/sections")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateListSectionRequest(text))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ListSectionDto.class)
                .returnResult()
                .getResponseBody();
        assertThat(created).isNotNull();
        return created;
    }

    private Long createItem(String token, String text, Boolean completed, Item.Type type, Long itemListId) {
        ItemDto created = client.post().uri("/items")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateItemRequest(text, completed, type, itemListId, null))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ItemDto.class)
                .returnResult()
                .getResponseBody();
        assertThat(created).isNotNull();
        return created.id();
    }
}
