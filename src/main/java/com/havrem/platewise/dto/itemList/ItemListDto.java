package com.havrem.platewise.dto.itemList;

import com.havrem.platewise.dto.category.CategoryDto;
import com.havrem.platewise.dto.item.ItemDto;

import java.util.List;

public record ItemListDto(Long id, String title, CategoryDto category, Boolean bookmarked, List<ItemDto> items) {
}
