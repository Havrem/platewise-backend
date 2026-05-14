package com.havrem.platewise.dto.itemList;

public record UpdateItemListRequest(String title, Long category, boolean bookmarked) {
}
