package com.havrem.platewise.controller;

import com.havrem.platewise.config.CurrentUser;
import com.havrem.platewise.dto.category.CreateCategoryRequest;
import com.havrem.platewise.dto.category.UpdateCategoryRequest;
import com.havrem.platewise.dto.category.CategoryDto;
import com.havrem.platewise.entity.User;
import com.havrem.platewise.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto create(@CurrentUser User user, @Valid @RequestBody CreateCategoryRequest request) {
        return categoryService.create(user, request);
    }

    @GetMapping("/{id}")
    public CategoryDto read(@CurrentUser User user, @PathVariable Long id) {
        return categoryService.read(user,id);
    }

    @GetMapping
    public List<CategoryDto> getAll(@CurrentUser User user) {
        return categoryService.readAll(user);
    }

    @PatchMapping("/{id}")
    public CategoryDto update(@CurrentUser User user, @PathVariable Long id, @Valid @RequestBody UpdateCategoryRequest request) {
        return categoryService.update(user, id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@CurrentUser User user, @PathVariable Long id) {
        categoryService.delete(user, id);
    }
}
