package com.havrem.platewise.dto.listSection;

public record ReorderListSectionRequest(
        Long previousId,
        Long nextId
) {
}
