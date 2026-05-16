package com.havrem.platewise.controller;

import com.havrem.platewise.dto.category.CategoryDto;
import com.havrem.platewise.dto.itemList.CreateItemListRequest;
import com.havrem.platewise.dto.itemList.ItemListDto;
import com.havrem.platewise.dto.itemList.UpdateItemListRequest;
import com.havrem.platewise.entity.Category;
import com.havrem.platewise.entity.User;
import com.havrem.platewise.exception.GlobalExceptionHandler;
import com.havrem.platewise.exception.NotFoundException;
import com.havrem.platewise.service.ItemListService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebMvcTest(ItemListController.class)
@AutoConfigureRestTestClient
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
public class ItemListControllerTest {
    @Autowired
    RestTestClient client;

    @MockitoBean
    ItemListService itemListService;

    User testUser;

    @BeforeEach
    void setupAuth() {
        testUser = new User("user@example.com", "hash");
        testUser.setId(1L);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())
        );
    }

    @AfterEach
    void clearAuth() {
        SecurityContextHolder.clearContext();
    }

    private ItemListDto sampleList(Long id, String title) {
        return new ItemListDto(
                id,
                title,
                new CategoryDto(3L, "Groceries", "shopping-cart", Category.Type.GROCERY),
                false,
                List.of()
        );
    }

    @Test
    void create_validRequest_returns201WithItemList() {
        when(itemListService.create(any(), any())).thenReturn(sampleList(10L, "Weekly groceries"));

        client.post().uri("/item-lists")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateItemListRequest("Weekly groceries", 3L))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo(10)
                .jsonPath("$.title").isEqualTo("Weekly groceries")
                .jsonPath("$.category.id").isEqualTo(3);
    }

    @Test
    void create_blankTitle_returns400() {
        client.post().uri("/item-lists")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateItemListRequest("", 3L))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void create_missingCategory_returns400() {
        client.post().uri("/item-lists")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateItemListRequest("Weekly groceries", null))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void getAll_returns200WithList() {
        when(itemListService.readAll(any())).thenReturn(List.of(
                sampleList(1L, "Weekly groceries"),
                sampleList(2L, "Party supplies")
        ));

        client.get().uri("/item-lists")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].title").isEqualTo("Weekly groceries");
    }

    @Test
    void getById_returns200() {
        when(itemListService.read(any(), eq(5L))).thenReturn(sampleList(5L, "Weekly groceries"));

        client.get().uri("/item-lists/5")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(5);
    }

    @Test
    void getById_missing_returns404() {
        when(itemListService.read(any(), eq(99L)))
                .thenThrow(NotFoundException.of("ItemList", 99L));

        client.get().uri("/item-lists/99")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void update_validRequest_returns200() {
        when(itemListService.update(any(), eq(5L), any()))
                .thenReturn(sampleList(5L, "Renamed"));

        client.put().uri("/item-lists/5")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new UpdateItemListRequest("Renamed", 3L, true))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Renamed");
    }

    @Test
    void delete_existing_returns204() {
        client.delete().uri("/item-lists/5")
                .exchange()
                .expectStatus().isNoContent();
    }
}
