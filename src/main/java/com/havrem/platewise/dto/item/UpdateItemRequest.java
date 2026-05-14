package com.havrem.platewise.dto.item;

import com.havrem.platewise.entity.Item;

public record UpdateItemRequest(String text, Boolean completed, Item.Type type) {
}
