package com.havrem.platewise.dto.itemList;

import com.havrem.platewise.dto.category.CategoryDto;
import com.havrem.platewise.dto.item.ItemDto;
import com.havrem.platewise.dto.listSection.ListSectionDto;
import com.havrem.platewise.entity.ItemList;

import java.util.List;

public record ItemListDto(Long id, String title, CategoryDto category, ItemList.Type type, Boolean bookmarked, List<ListSectionDto> sections, List<ItemDto> items) {
}
