package com.havrem.platewise.dto.response;

import com.havrem.platewise.entity.Item;

public record ItemDto(Long id, Item.Type type, String text, Boolean isDone) {
}
