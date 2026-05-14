package com.havrem.platewise.dto.category;

import com.havrem.platewise.entity.Category;

public record CreateCategoryRequest(String name, String icon, Category.Type type) {
}
