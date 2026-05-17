package com.havrem.platewise.service;

import java.util.List;

public record GroceryOrganization(List<Section> sections) {
    public record Section(String text, List<Long> itemIds) {
    }
}
