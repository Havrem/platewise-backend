package com.havrem.platewise.controller;

import com.havrem.platewise.config.CurrentUser;
import com.havrem.platewise.dto.listSection.CreateListSectionRequest;
import com.havrem.platewise.dto.listSection.ListSectionDto;
import com.havrem.platewise.dto.listSection.ReorderListSectionRequest;
import com.havrem.platewise.dto.listSection.UpdateListSectionRequest;
import com.havrem.platewise.entity.User;
import com.havrem.platewise.service.ListSectionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class ListSectionController {
    private final ListSectionService sectionService;

    public ListSectionController(ListSectionService sectionService) {
        this.sectionService = sectionService;
    }

    @PostMapping("/item-lists/{listId}/sections")
    @ResponseStatus(HttpStatus.CREATED)
    public ListSectionDto create(@CurrentUser User user, @PathVariable Long listId, @Valid @RequestBody CreateListSectionRequest request) {
        return sectionService.create(user, listId, request);
    }

    @PatchMapping("/list-sections/{id}")
    public ListSectionDto update(@CurrentUser User user, @PathVariable Long id, @Valid @RequestBody UpdateListSectionRequest request) {
        return sectionService.update(user, id, request);
    }

    @PatchMapping("/list-sections/{id}/order")
    public ListSectionDto reorder(@CurrentUser User user, @PathVariable Long id, @RequestBody ReorderListSectionRequest request) {
        return sectionService.reorder(user, id, request);
    }

    @DeleteMapping("/list-sections/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@CurrentUser User user, @PathVariable Long id) {
        sectionService.delete(user, id);
    }
}
