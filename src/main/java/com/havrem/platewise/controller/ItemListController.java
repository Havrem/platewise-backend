package com.havrem.platewise.controller;

import com.havrem.platewise.config.CurrentUser;
import com.havrem.platewise.dto.itemList.CreateItemListRequest;
import com.havrem.platewise.dto.itemList.UpdateItemListRequest;
import com.havrem.platewise.dto.itemList.ItemListDto;
import com.havrem.platewise.entity.User;
import com.havrem.platewise.service.ItemListService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/item-lists")
public class ItemListController {
    private final ItemListService itemListService;

    public ItemListController(ItemListService itemListService) {
        this.itemListService = itemListService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemListDto create(@CurrentUser User user, @Valid @RequestBody CreateItemListRequest request) {
        return itemListService.create(user, request);
    }

    @GetMapping("/{id}")
    public ItemListDto read(@CurrentUser User user, @PathVariable Long id) {
        return itemListService.read(user, id);
    }

    @GetMapping
    public List<ItemListDto> readAll(@CurrentUser User user) {
        return itemListService.readAll(user);
    }

    @PatchMapping("/{id}")
    public ItemListDto update(@CurrentUser User user, @PathVariable Long id, @Valid @RequestBody UpdateItemListRequest request) {
        return itemListService.update(user, id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@CurrentUser User user, @PathVariable Long id) {
        itemListService.delete(user, id);
    }
}
