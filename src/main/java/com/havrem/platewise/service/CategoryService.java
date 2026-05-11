package com.havrem.platewise.service;

import com.havrem.platewise.entity.Category;
import com.havrem.platewise.exception.NotFoundException;
import com.havrem.platewise.repository.CategoryRepository;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category findById(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> NotFoundException.of("Category", id));
    }
}
