package com.havrem.platewise.dto.category;

import com.havrem.platewise.entity.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCategoryRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 100) String icon,
        @NotNull Category.Type type
) {
}
