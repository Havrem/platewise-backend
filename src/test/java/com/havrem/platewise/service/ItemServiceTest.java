package com.havrem.platewise.service;

import com.havrem.platewise.dto.item.CreateItemRequest;
import com.havrem.platewise.dto.item.ItemDto;
import com.havrem.platewise.entity.Item;
import com.havrem.platewise.entity.ItemList;
import com.havrem.platewise.entity.User;
import com.havrem.platewise.exception.NotFoundException;
import com.havrem.platewise.mapper.ItemMapper;
import com.havrem.platewise.repository.ItemRepository;
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

class ItemServiceTest {
    private ItemRepository itemRepository;
    private ItemMapper itemMapper;
    private ItemListService itemListService;
    private ItemService itemService;

    private User user;

    @BeforeEach
    void setup() {
        itemRepository = mock(ItemRepository.class);
        itemMapper = mock(ItemMapper.class);
        itemListService = mock(ItemListService.class);
        itemService = new ItemService(itemRepository, itemMapper, itemListService);

        user = new User("a@b.com", "hash");
        user.setId(42L);
    }

    @Test
    void find_queriesByCurrentUserIdAndThrowsWhenAbsent() {
        when(itemRepository.findByUserIdAndId(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.find(user, 99L))
                .isInstanceOf(NotFoundException.class);

        verify(itemRepository).findByUserIdAndId(42L, 99L);
    }

    @Test
    void readAll_filtersByCurrentUserId() {
        when(itemRepository.findAllByUserId(42L)).thenReturn(java.util.List.of());

        itemService.readAll(user);

        verify(itemRepository).findAllByUserId(42L);
    }

    @Test
    void create_otherUsersItemList_throwsNotFoundWithoutSaving() {
        when(itemListService.find(user, 7L)).thenThrow(NotFoundException.of("ItemList", 7L));

        assertThatThrownBy(() -> itemService.create(user, new CreateItemRequest("Milk", false, Item.Type.CHECKED, 7L)))
                .isInstanceOf(NotFoundException.class);

        verify(itemRepository, never()).save(any());
    }

    @Test
    void create_persistsItemOwnedByCurrentUserAndLinkedToList() {
        ItemList parent = mock(ItemList.class);
        when(itemListService.find(user, 7L)).thenReturn(parent);
        when(itemMapper.toDto(any())).thenReturn(new ItemDto(1L, 7L, Item.Type.CHECKED, "Milk", false));

        itemService.create(user, new CreateItemRequest("Milk", false, Item.Type.CHECKED, 7L));

        ArgumentCaptor<Item> captor = ArgumentCaptor.forClass(Item.class);
        verify(itemRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isEqualTo(user);
        assertThat(captor.getValue().getItemList()).isEqualTo(parent);
        assertThat(captor.getValue().getText()).isEqualTo("Milk");
    }
}
