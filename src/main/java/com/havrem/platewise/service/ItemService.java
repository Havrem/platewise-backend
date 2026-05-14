package com.havrem.platewise.service;

import com.havrem.platewise.dto.item.CreateItemRequest;
import com.havrem.platewise.dto.item.ItemDto;
import com.havrem.platewise.dto.item.UpdateItemRequest;
import com.havrem.platewise.entity.Item;
import com.havrem.platewise.entity.ItemList;
import com.havrem.platewise.entity.User;
import com.havrem.platewise.exception.NotFoundException;
import com.havrem.platewise.mapper.ItemMapper;
import com.havrem.platewise.repository.ItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemService {
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final ItemListService itemListService;

    public ItemService(ItemRepository itemRepository, ItemMapper itemMapper, ItemListService itemListService) {
        this.itemRepository = itemRepository;
        this.itemMapper = itemMapper;
        this.itemListService = itemListService;
    }

    public Item find(User user, Long id) {
        return itemRepository.findByUserIdAndId(user.getId(), id).orElseThrow(() -> NotFoundException.of("Item", id));
    }

    public ItemDto create(User user, CreateItemRequest request) {
        ItemList itemList = itemListService.find(user, request.itemListId());

        Item item = new Item(request.text(), request.completed(), itemList, user, request.type());

        itemRepository.save(item);

        return itemMapper.toDto(item);
    }

    public ItemDto read(User user, Long id) {
        Item item = find(user, id);

        return itemMapper.toDto(item);
    }

    public List<ItemDto> readAll(User user) {
        List<Item> items = itemRepository.findAllByUserId(user.getId());

        return itemMapper.toDtos(items);
    }

    public ItemDto update(User user, Long id, UpdateItemRequest request) {
        Item item = find(user, id);

        item.setText(request.text());
        item.setCompleted(request.completed());
        item.setType(request.type());

        itemRepository.save(item);

        return itemMapper.toDto(item);
    }

    public void delete(User user, Long id) {
        Item item = find(user, id);

        itemRepository.delete(item);
    }
}
