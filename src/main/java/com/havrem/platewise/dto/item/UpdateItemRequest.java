package com.havrem.platewise.dto.item;

import com.havrem.platewise.entity.Item;
import jakarta.validation.constraints.Size;

public record UpdateItemRequest(
        @Size(max = 500) String text,
        Boolean completed,
        Item.Type type
) {
}
