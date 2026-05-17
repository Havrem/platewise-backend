package com.havrem.platewise.dto.itemList;

public record ReorderItemListRequest(
        Long previousId,
        Long nextId
) {
}
