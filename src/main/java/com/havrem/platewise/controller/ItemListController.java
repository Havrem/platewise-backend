package com.havrem.platewise.controller;

import com.havrem.platewise.config.CurrentUser;
import com.havrem.platewise.dto.request.CreateItemListRequest;
import com.havrem.platewise.dto.request.UpdateItemListRequest;
import com.havrem.platewise.dto.response.ItemListDto;
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

    @GetMapping("/{id}")
    public ItemListDto get(@PathVariable Long id) {
        return itemListService.findById(id);
    }

    @GetMapping
    public List<ItemListDto> getAll(@CurrentUser User user) {
        return itemListService.findAllByUser(user);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemListDto create(@Valid @RequestBody CreateItemListRequest request) {
        return itemListService.create(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        itemListService.deleteById(id);
    }

    @PutMapping("/{id}")
    public ItemListDto update(@PathVariable Long id, @Valid @RequestBody UpdateItemListRequest request) {
        return itemListService.update(id, request);
    }
}
