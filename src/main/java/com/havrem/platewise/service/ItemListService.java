package com.havrem.platewise.service;

import com.havrem.platewise.config.CurrentUser;
import com.havrem.platewise.dto.request.CreateItemListRequest;
import com.havrem.platewise.dto.request.UpdateItemListRequest;
import com.havrem.platewise.dto.response.ItemListDto;
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

    public ItemListDto findById(Long id) {
        ItemList itemList = itemListRepository.findById(id).orElseThrow(() -> NotFoundException.of("ItemList", id));

        return itemListMapper.toDto(itemList);
    }

    public List<ItemListDto> findAllByUser(@CurrentUser User user) {
        List<ItemList> itemLists = itemListRepository.findAllByCategoryUserId(user.getId());

        return itemListMapper.toDtos(itemLists);
    }

    public ItemListDto create(CreateItemListRequest request) {
        Category category = categoryService.findById(request.category());

        ItemList itemList = new ItemList(request.title(), category, false);

        itemListRepository.save(itemList);

        return itemListMapper.toDto(itemList);
    }

    public void deleteById(Long id) {
        itemListRepository.deleteById(id);
    }

    public ItemListDto update(Long id, UpdateItemListRequest request) {
        ItemList itemList = itemListRepository.findById(id).orElseThrow(() -> NotFoundException.of("Itemlist", id));

        Category category = categoryService.findById(request.category());

        itemList.setTitle(request.title());
        itemList.setCategory(category);
        itemList.setBookmarked(request.bookmarked());

        itemListRepository.save(itemList);

        return itemListMapper.toDto(itemList);
    }
}
