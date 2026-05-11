package com.havrem.platewise.controller;

import com.havrem.platewise.dto.request.CreateItemListRequest;
import com.havrem.platewise.dto.request.UpdateItemListRequest;
import com.havrem.platewise.dto.response.ItemListDto;
import com.havrem.platewise.service.ItemListService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
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
    public List<ItemListDto> getAll() {
        return itemListService.findAll();
    }

    @PostMapping()
    public ItemListDto create(@Valid @RequestBody CreateItemListRequest request) {
        return itemListService.create(request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        itemListService.deleteById(id);
    }

    @PostMapping("/{id}")
    public ItemListDto update(@PathVariable Long id, @Valid @RequestBody UpdateItemListRequest request) {
        itemListService.update(id, request);
    }
}
