package com.havrem.platewise.dto.response;

import com.havrem.platewise.entity.Category;

import java.util.List;

public record ItemListDto(Long id, String title, Category category, Boolean bookmarked, List<ItemDto> items) {
}
