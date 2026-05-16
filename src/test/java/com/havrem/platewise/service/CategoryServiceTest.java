package com.havrem.platewise.service;

import com.havrem.platewise.dto.category.CategoryDto;
import com.havrem.platewise.dto.category.CreateCategoryRequest;
import com.havrem.platewise.entity.Category;
import com.havrem.platewise.entity.User;
import com.havrem.platewise.exception.NotFoundException;
import com.havrem.platewise.mapper.CategoryMapper;
import com.havrem.platewise.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CategoryServiceTest {
    private CategoryRepository categoryRepository;
    private CategoryMapper categoryMapper;
    private CategoryService categoryService;

    private User user;

    @BeforeEach
    void setup() {
        categoryRepository = mock(CategoryRepository.class);
        categoryMapper = mock(CategoryMapper.class);
        categoryService = new CategoryService(categoryRepository, categoryMapper);

        user = new User("a@b.com", "hash");
        user.setId(42L);
    }

    @Test
    void find_queriesByCurrentUserIdAndThrowsWhenAbsent() {
        when(categoryRepository.findByUserIdAndId(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.find(user, 99L))
                .isInstanceOf(NotFoundException.class);

        verify(categoryRepository).findByUserIdAndId(42L, 99L);
    }

    @Test
    void readAll_filtersByCurrentUserId() {
        when(categoryRepository.findAllByUserId(42L)).thenReturn(java.util.List.of());

        categoryService.readAll(user);

        verify(categoryRepository).findAllByUserId(42L);
    }

    @Test
    void create_persistsCategoryOwnedByCurrentUser() {
        when(categoryMapper.toDto(any())).thenReturn(new CategoryDto(1L, "Groceries", "icon", Category.Type.GROCERY));

        categoryService.create(user, new CreateCategoryRequest("Groceries", "icon", Category.Type.GROCERY));

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isEqualTo(user);
        assertThat(captor.getValue().getName()).isEqualTo("Groceries");
    }
}
