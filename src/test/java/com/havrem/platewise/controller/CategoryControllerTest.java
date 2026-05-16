package com.havrem.platewise.controller;

import com.havrem.platewise.dto.category.CategoryDto;
import com.havrem.platewise.dto.category.CreateCategoryRequest;
import com.havrem.platewise.dto.category.UpdateCategoryRequest;
import com.havrem.platewise.entity.Category;
import com.havrem.platewise.entity.User;
import com.havrem.platewise.exception.GlobalExceptionHandler;
import com.havrem.platewise.exception.NotFoundException;
import com.havrem.platewise.service.CategoryService;
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

@WebMvcTest(CategoryController.class)
@AutoConfigureRestTestClient
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
public class CategoryControllerTest {
    @Autowired
    RestTestClient client;

    @MockitoBean
    CategoryService categoryService;

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
    void create_validRequest_returns201WithCategory() {
        when(categoryService.create(any(), any()))
                .thenReturn(new CategoryDto(10L, "Groceries", "shopping-cart", Category.Type.GROCERY));

        client.post().uri("/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateCategoryRequest("Groceries", "shopping-cart", Category.Type.GROCERY))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo(10)
                .jsonPath("$.name").isEqualTo("Groceries")
                .jsonPath("$.type").isEqualTo("GROCERY");
    }

    @Test
    void create_blankName_returns400() {
        client.post().uri("/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateCategoryRequest("", "shopping-cart", Category.Type.GROCERY))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void create_invalidType_returns400() {
        client.post().uri("/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                            "name": "Groceries",
                            "icon": "shopping-cart",
                            "type": "NOT_A_REAL_TYPE"
                        }
                        """)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void getAll_returns200WithList() {
        when(categoryService.readAll(any())).thenReturn(List.of(
                new CategoryDto(1L, "Groceries", "icon-1", Category.Type.GROCERY),
                new CategoryDto(2L, "Recipes", "icon-2", Category.Type.RECIPES)
        ));

        client.get().uri("/categories")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].name").isEqualTo("Groceries");
    }

    @Test
    void getById_returns200() {
        when(categoryService.read(any(), eq(5L)))
                .thenReturn(new CategoryDto(5L, "Groceries", "icon", Category.Type.GROCERY));

        client.get().uri("/categories/5")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(5);
    }

    @Test
    void getById_missing_returns404() {
        when(categoryService.read(any(), eq(99L)))
                .thenThrow(NotFoundException.of("Category", 99L));

        client.get().uri("/categories/99")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void update_validRequest_returns200() {
        when(categoryService.update(any(), eq(5L), any()))
                .thenReturn(new CategoryDto(5L, "Renamed", "icon", Category.Type.GROCERY));

        client.put().uri("/categories/5")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new UpdateCategoryRequest("Renamed", "icon", Category.Type.GROCERY))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Renamed");
    }

    @Test
    void delete_existing_returns204() {
        client.delete().uri("/categories/5")
                .exchange()
                .expectStatus().isNoContent();
    }
}
