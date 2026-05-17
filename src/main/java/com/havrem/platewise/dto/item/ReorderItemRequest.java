package com.havrem.platewise.dto.item;

public record ReorderItemRequest(
        Long previousId,
        Long nextId
) {
}
