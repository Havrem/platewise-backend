package com.havrem.platewise.dto.item;

import com.havrem.platewise.entity.Item;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateItemRequest(
        @Size(max = 500) String text,
        @NotNull Boolean completed,
        @NotNull Item.Type type,
        @NotNull Long itemListId,
        Long sectionId
) {
}
