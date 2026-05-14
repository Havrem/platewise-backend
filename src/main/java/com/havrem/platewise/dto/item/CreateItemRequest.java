package com.havrem.platewise.dto.item;

import com.havrem.platewise.entity.Item;

public record CreateItemRequest(String text, Boolean completed, Item.Type type, Long itemListId) {
}
