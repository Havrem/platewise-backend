package com.havrem.platewise.service;

import com.havrem.platewise.dto.category.CreateCategoryRequest;
import com.havrem.platewise.dto.category.UpdateCategoryRequest;
import com.havrem.platewise.dto.category.CategoryDto;
import com.havrem.platewise.entity.Category;
import com.havrem.platewise.entity.User;
import com.havrem.platewise.exception.BadRequestException;
import com.havrem.platewise.exception.NotFoundException;
import com.havrem.platewise.mapper.CategoryMapper;
import com.havrem.platewise.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    public Category find(User user, Long id) {
        return categoryRepository.findByUserIdAndId(user.getId(), id).orElseThrow(() -> NotFoundException.of("Category", id));
    }

    public CategoryDto create(User user, CreateCategoryRequest request) {
        Category category = new Category(request.name(), request.icon(), user, request.type());

        categoryRepository.save(category);

        return categoryMapper.toDto(category);
    }

    public CategoryDto read(User user, Long id) {
        Category category = find(user, id);

        return categoryMapper.toDto(category);
    }

    public List<CategoryDto> readAll(User user) {
        List<Category> categories = categoryRepository.findAllByUserId(user.getId());

        return categoryMapper.toDtos(categories);
    }

    public CategoryDto update(User user, Long id, UpdateCategoryRequest request) {
        Category category = find(user, id);
        requireNotShared(category);

        categoryMapper.update(category, request);

        categoryRepository.save(category);

        return categoryMapper.toDto(category);
    }

    public void delete(User user, Long id) {
        Category category = find(user, id);
        requireNotShared(category);

        categoryRepository.delete(category);
    }

    private void requireNotShared(Category category) {
        if (category.getKind() == Category.Kind.SHARED) {
            throw new BadRequestException("The Shared category is managed automatically and cannot be modified.");
        }
    }
}
