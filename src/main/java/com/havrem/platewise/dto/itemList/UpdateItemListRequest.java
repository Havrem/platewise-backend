package com.havrem.platewise.dto.itemList;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateItemListRequest(
        @NotBlank @Size(max = 500) String title,
        @NotNull Long category,
        boolean bookmarked
) {
}
