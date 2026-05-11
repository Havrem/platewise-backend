package com.havrem.platewise.mapper;

import com.havrem.platewise.dto.response.CategoryDto;
import com.havrem.platewise.entity.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryDto toDto(Category category);
}
