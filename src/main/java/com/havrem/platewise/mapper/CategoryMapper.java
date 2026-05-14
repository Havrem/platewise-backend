package com.havrem.platewise.mapper;

import com.havrem.platewise.dto.category.CategoryDto;
import com.havrem.platewise.entity.Category;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryDto toDto(Category category);
    List<CategoryDto> toDtos(List<Category> categories);
}
