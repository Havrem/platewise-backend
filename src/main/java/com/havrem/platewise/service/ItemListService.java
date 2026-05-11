package com.havrem.platewise.service;

import com.havrem.platewise.dto.response.ItemListDto;
import com.havrem.platewise.entity.ItemList;
import com.havrem.platewise.exception.NotFoundException;
import com.havrem.platewise.repository.ItemListRepository;
import org.springframework.stereotype.Service;

@Service
public class ItemListService {
    private final ItemListRepository itemListRepository;

    public ItemListService(ItemListRepository itemListRepository) {
        this.itemListRepository = itemListRepository;
    }

    public ItemListDto findById(Long id) {
        ItemList itemList = itemListRepository.findById(id).orElseThrow(() -> NotFoundException.of("ItemList", id));

        return itemList;
    }
}
