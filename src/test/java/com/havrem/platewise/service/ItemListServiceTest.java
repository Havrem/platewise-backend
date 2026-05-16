package com.havrem.platewise.service;

import com.havrem.platewise.dto.itemList.CreateItemListRequest;
import com.havrem.platewise.dto.itemList.ItemListDto;
import com.havrem.platewise.entity.Category;
import com.havrem.platewise.entity.ItemList;
import com.havrem.platewise.entity.User;
import com.havrem.platewise.exception.NotFoundException;
import com.havrem.platewise.mapper.ItemListMapper;
import com.havrem.platewise.repository.ItemListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ItemListServiceTest {
    private ItemListRepository itemListRepository;
    private ItemListMapper itemListMapper;
    private CategoryService categoryService;
    private ItemListService itemListService;

    private User user;

    @BeforeEach
    void setup() {
        itemListRepository = mock(ItemListRepository.class);
        itemListMapper = mock(ItemListMapper.class);
        categoryService = mock(CategoryService.class);
        itemListService = new ItemListService(itemListRepository, itemListMapper, categoryService);

        user = new User("a@b.com", "hash");
        user.setId(42L);
    }

    @Test
    void find_queriesByCurrentUserIdAndThrowsWhenAbsent() {
        when(itemListRepository.findByUserIdAndId(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemListService.find(user, 99L))
                .isInstanceOf(NotFoundException.class);

        verify(itemListRepository).findByUserIdAndId(42L, 99L);
    }

    @Test
    void readAll_filtersByCurrentUserId() {
        when(itemListRepository.findAllByUserId(42L)).thenReturn(java.util.List.of());

        itemListService.readAll(user);

        verify(itemListRepository).findAllByUserId(42L);
    }

    @Test
    void create_otherUsersCategory_throwsNotFoundWithoutSaving() {
        when(categoryService.find(user, 3L)).thenThrow(NotFoundException.of("Category", 3L));

        assertThatThrownBy(() -> itemListService.create(user, new CreateItemListRequest("Weekly", 3L)))
                .isInstanceOf(NotFoundException.class);

        verify(itemListRepository, never()).save(any());
    }

    @Test
    void create_persistsListOwnedByCurrentUserAndLinkedToCategory() {
        Category parent = mock(Category.class);
        when(categoryService.find(user, 3L)).thenReturn(parent);
        when(itemListMapper.toDto(any())).thenReturn(new ItemListDto(1L, "Weekly", null, false, java.util.List.of()));

        itemListService.create(user, new CreateItemListRequest("Weekly", 3L));

        ArgumentCaptor<ItemList> captor = ArgumentCaptor.forClass(ItemList.class);
        verify(itemListRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isEqualTo(user);
        assertThat(captor.getValue().getCategory()).isEqualTo(parent);
        assertThat(captor.getValue().getTitle()).isEqualTo("Weekly");
    }
}
