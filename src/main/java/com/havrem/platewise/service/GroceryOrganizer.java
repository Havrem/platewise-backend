package com.havrem.platewise.service;

import com.havrem.platewise.entity.Item;
import com.havrem.platewise.entity.ItemList;
import com.havrem.platewise.entity.ListSection;

import java.util.List;

public interface GroceryOrganizer {
    GroceryOrganization organize(ItemList list, List<Item> items, List<ListSection> existingSections);
}
