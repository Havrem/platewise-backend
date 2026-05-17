package com.havrem.platewise.dto.itemList;

import com.havrem.platewise.entity.ItemList;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateItemListRequest(
        @NotBlank @Size(max = 500) String title,
        @NotNull Long category,
        @NotNull ItemList.Type type
) {
}
