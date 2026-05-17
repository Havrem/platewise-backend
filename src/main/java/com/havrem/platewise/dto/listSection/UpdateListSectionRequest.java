package com.havrem.platewise.dto.listSection;

import jakarta.validation.constraints.Size;

public record UpdateListSectionRequest(
        @Size(max = 500) String text
) {
}
