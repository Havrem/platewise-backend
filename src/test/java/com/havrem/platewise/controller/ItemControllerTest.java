package com.havrem.platewise.controller;

import com.havrem.platewise.dto.item.CreateItemRequest;
import com.havrem.platewise.dto.item.ItemDto;
import com.havrem.platewise.dto.item.UpdateItemRequest;
import com.havrem.platewise.entity.Item;
import com.havrem.platewise.entity.User;
import com.havrem.platewise.exception.GlobalExceptionHandler;
import com.havrem.platewise.exception.NotFoundException;
import com.havrem.platewise.service.ItemService;
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

@WebMvcTest(ItemController.class)
@AutoConfigureRestTestClient
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
public class ItemControllerTest {
    @Autowired
    RestTestClient client;

    @MockitoBean
    ItemService itemService;

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

    @Test
    void create_validRequest_returns201WithItem() {
        when(itemService.create(any(), any()))
                .thenReturn(new ItemDto(10L, 7L, Item.Type.CHECKED, "Milk", false));

        client.post().uri("/items")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateItemRequest("Milk", false, Item.Type.CHECKED, 7L))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo(10)
                .jsonPath("$.text").isEqualTo("Milk")
                .jsonPath("$.type").isEqualTo("CHECKED");
    }

    @Test
    void create_missingItemListId_returns400() {
        client.post().uri("/items")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateItemRequest("Milk", false, Item.Type.CHECKED, null))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void create_invalidType_returns400() {
        client.post().uri("/items")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                            "text": "Milk",
                            "completed": false,
                            "type": "NOT_A_REAL_TYPE",
                            "itemListId": 7
                        }
                        """)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void getAll_returns200WithList() {
        when(itemService.readAll(any())).thenReturn(List.of(
                new ItemDto(1L, 7L, Item.Type.CHECKED, "Milk", false),
                new ItemDto(2L, 7L, Item.Type.CHECKED, "Eggs", true)
        ));

        client.get().uri("/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[1].completed").isEqualTo(true);
    }

    @Test
    void getById_returns200() {
        when(itemService.read(any(), eq(5L)))
                .thenReturn(new ItemDto(5L, 7L, Item.Type.CHECKED, "Milk", false));

        client.get().uri("/items/5")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(5);
    }

    @Test
    void getById_missing_returns404() {
        when(itemService.read(any(), eq(99L)))
                .thenThrow(NotFoundException.of("Item", 99L));

        client.get().uri("/items/99")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void update_validRequest_returns200() {
        when(itemService.update(any(), eq(5L), any()))
                .thenReturn(new ItemDto(5L, 7L, Item.Type.CHECKED, "Whole milk", true));

        client.put().uri("/items/5")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new UpdateItemRequest("Whole milk", true, Item.Type.CHECKED))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.text").isEqualTo("Whole milk")
                .jsonPath("$.completed").isEqualTo(true);
    }

    @Test
    void delete_existing_returns204() {
        client.delete().uri("/items/5")
                .exchange()
                .expectStatus().isNoContent();
    }
}
