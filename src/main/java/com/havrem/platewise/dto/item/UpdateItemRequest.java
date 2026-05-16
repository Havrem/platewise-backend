package com.havrem.platewise.dto.item;

import com.havrem.platewise.entity.Item;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateItemRequest(
        @Size(max = 500) String text,
        @NotNull Boolean completed,
        @NotNull Item.Type type
) {
}
