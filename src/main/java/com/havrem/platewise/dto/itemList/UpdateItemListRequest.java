package com.havrem.platewise.dto.itemList;

import jakarta.validation.constraints.Size;

public record UpdateItemListRequest(
        @Size(max = 500) String title,
        Long category,
        Boolean bookmarked
) {
}
