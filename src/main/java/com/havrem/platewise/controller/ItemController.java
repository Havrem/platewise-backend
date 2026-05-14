package com.havrem.platewise.controller;

import com.havrem.platewise.config.CurrentUser;
import com.havrem.platewise.dto.item.CreateItemRequest;
import com.havrem.platewise.dto.item.ItemDto;
import com.havrem.platewise.dto.item.UpdateItemRequest;
import com.havrem.platewise.entity.User;
import com.havrem.platewise.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto create(@CurrentUser User user, @Valid @RequestBody CreateItemRequest request) {
        return itemService.create(user, request);
    }

    @GetMapping("/{id}")
    public ItemDto read(@CurrentUser User user, @PathVariable Long id) {
        return itemService.read(user,id);
    }

    @GetMapping
    public List<ItemDto> readAll(@CurrentUser User user) {
        return itemService.readAll(user);
    }

    @PutMapping("/{id}")
    public ItemDto update(@CurrentUser User user, @PathVariable Long id, @Valid @RequestBody UpdateItemRequest request) {
        return itemService.update(user, id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@CurrentUser User user, @PathVariable Long id) {
        itemService.delete(user, id);
    }
}
