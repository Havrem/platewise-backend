package com.havrem.platewise.dto.category;

import jakarta.validation.constraints.Size;

public record UpdateCategoryRequest(
        @Size(max = 100) String name,
        @Size(max = 100) String icon
) {
}
