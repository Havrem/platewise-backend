package com.havrem.platewise.controller;

import com.havrem.platewise.config.CurrentUser;
import com.havrem.platewise.dto.invite.CreateInviteRequest;
import com.havrem.platewise.dto.invite.InviteDto;
import com.havrem.platewise.dto.itemList.CreateItemListRequest;
import com.havrem.platewise.dto.itemList.ImportItemsRequest;
import com.havrem.platewise.dto.itemList.ItemListDto;
import com.havrem.platewise.dto.itemList.ReorderItemListRequest;
import com.havrem.platewise.dto.itemList.UpdateItemListRequest;
import com.havrem.platewise.entity.User;
import com.havrem.platewise.service.ItemListService;
import com.havrem.platewise.service.ListInviteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/item-lists")
public class ItemListController {
    private final ItemListService itemListService;
    private final ListInviteService inviteService;

    public ItemListController(ItemListService itemListService, ListInviteService inviteService) {
        this.itemListService = itemListService;
        this.inviteService = inviteService;
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

    @PatchMapping("/{id}/order")
    public ItemListDto reorder(@CurrentUser User user, @PathVariable Long id, @RequestBody ReorderItemListRequest request) {
        return itemListService.reorder(user, id, request);
    }

    @PostMapping("/{id}/items/import")
    public ItemListDto importItems(@CurrentUser User user, @PathVariable Long id, @Valid @RequestBody ImportItemsRequest request) {
        return itemListService.importItems(user, id, request);
    }

    @PostMapping("/{id}/sections/organize")
    public ItemListDto organizeGrocerySections(@CurrentUser User user, @PathVariable Long id) {
        return itemListService.organizeGrocerySections(user, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@CurrentUser User user, @PathVariable Long id) {
        itemListService.delete(user, id);
    }

    @PostMapping("/{id}/invites")
    @ResponseStatus(HttpStatus.CREATED)
    public InviteDto invite(@CurrentUser User user, @PathVariable Long id, @Valid @RequestBody CreateInviteRequest request) {
        return inviteService.invite(user, id, request);
    }
}
