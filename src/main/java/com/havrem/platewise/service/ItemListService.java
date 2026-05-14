package com.havrem.platewise.service;

import com.havrem.platewise.dto.itemList.CreateItemListRequest;
import com.havrem.platewise.dto.itemList.UpdateItemListRequest;
import com.havrem.platewise.dto.itemList.ItemListDto;
import com.havrem.platewise.entity.Category;
import com.havrem.platewise.entity.ItemList;
import com.havrem.platewise.entity.User;
import com.havrem.platewise.exception.NotFoundException;
import com.havrem.platewise.mapper.ItemListMapper;
import com.havrem.platewise.repository.ItemListRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemListService {
    private final ItemListRepository itemListRepository;
    private final ItemListMapper itemListMapper;
    private final CategoryService categoryService;

    public ItemListService(ItemListRepository itemListRepository, ItemListMapper itemListMapper, CategoryService categoryService) {
        this.itemListRepository = itemListRepository;
        this.itemListMapper = itemListMapper;
        this.categoryService = categoryService;
    }

    public ItemList find(User user, Long id) {
        return itemListRepository.findByUserIdAndId(user.getId(), id).orElseThrow(() -> NotFoundException.of("ItemList", id));
    }

    public ItemListDto read(User user, Long id) {
        return itemListMapper.toDto(find(user, id));
    }

    public List<ItemListDto> readAll(User user) {
        List<ItemList> itemLists = itemListRepository.findAllByUserId(user.getId());

        return itemListMapper.toDtos(itemLists);
    }

    public ItemListDto create(User user,  CreateItemListRequest request) {
        Category category = categoryService.find(user, request.category());

        ItemList itemList = new ItemList(request.title(), category, false, user);

        itemListRepository.save(itemList);

        return itemListMapper.toDto(itemList);
    }

    public void delete(User user, Long id) {
        ItemList itemList = find(user, id);

        itemListRepository.delete(itemList);
    }

    public ItemListDto update(User user, Long id, UpdateItemListRequest request) {
        ItemList itemList = find(user, id);

        Category category = categoryService.find(user, request.category());

        itemList.setTitle(request.title());
        itemList.setCategory(category);
        itemList.setBookmarked(request.bookmarked());

        itemListRepository.save(itemList);

        return itemListMapper.toDto(itemList);
    }
}
