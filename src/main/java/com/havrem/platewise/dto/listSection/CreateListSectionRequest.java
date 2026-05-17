package com.havrem.platewise.dto.listSection;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateListSectionRequest(
        @NotBlank @Size(max = 500) String text
) {
}
