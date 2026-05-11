package com.havrem.platewise.dto.response;

import java.util.List;

public record ItemListDto(Long id, String title, Long category, Boolean bookmarked, List<ItemDto> items) {
}
