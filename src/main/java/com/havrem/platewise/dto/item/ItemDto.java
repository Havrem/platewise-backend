package com.havrem.platewise.dto.item;

import com.havrem.platewise.entity.Item;

public record ItemDto(Long id, Long itemListId, Long sectionId, Item.Type type, String text, Boolean completed) {
}
