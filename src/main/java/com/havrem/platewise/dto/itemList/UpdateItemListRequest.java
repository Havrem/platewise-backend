package com.havrem.platewise.dto.itemList;

import com.havrem.platewise.entity.ItemList;
import jakarta.validation.constraints.Size;

public record UpdateItemListRequest(
        @Size(max = 500) String title,
        Long category,
        ItemList.Type type,
        Boolean bookmarked
) {
}
