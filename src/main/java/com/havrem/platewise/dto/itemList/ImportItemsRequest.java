package com.havrem.platewise.dto.itemList;

import jakarta.validation.constraints.NotNull;

public record ImportItemsRequest(
        @NotNull Long sourceListId
) {
}
