package com.havrem.platewise.service;

import com.havrem.platewise.dto.itemList.CreateItemListRequest;
import com.havrem.platewise.dto.itemList.ItemListDto;
import com.havrem.platewise.dto.itemList.ReorderItemListRequest;
import com.havrem.platewise.dto.itemList.UpdateItemListRequest;
import com.havrem.platewise.entity.Category;
import com.havrem.platewise.entity.ItemList;
import com.havrem.platewise.entity.User;
import com.havrem.platewise.exception.BadRequestException;
import com.havrem.platewise.exception.NotFoundException;
import com.havrem.platewise.mapper.ItemListMapper;
import com.havrem.platewise.repository.ItemListRepository;
import com.havrem.platewise.util.LexoRank;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemListService {
    private final ItemListRepository itemListRepository;
    private final ItemListMapper itemListMapper;
    private final CategoryService categoryService;

    public ItemListService(ItemListRepository itemListRepository, ItemListMapper itemListMapper, CategoryService categoryService) {
        this.itemListRepository = itemListRepository;
        this.itemListMapper = itemListMapper;
        this.categoryService = categoryService;
    }

    public ItemList find(User user, Long id) {
        return itemListRepository.findByUserIdAndId(user.getId(), id).orElseThrow(() -> NotFoundException.of("ItemList", id));
    }

    public ItemListDto read(User user, Long id) {
        return itemListMapper.toDto(find(user, id));
    }

    public List<ItemListDto> readAll(User user) {
        List<ItemList> itemLists = itemListRepository.findAllByUserIdOrderByRankAsc(user.getId());

        return itemListMapper.toDtos(itemLists);
    }

    public ItemListDto create(User user, CreateItemListRequest request) {
        Category category = categoryService.find(user, request.category());

        String lastRank = itemListRepository
                .findFirstByUserIdAndCategoryIdOrderByRankDesc(user.getId(), category.getId())
                .map(ItemList::getRank)
                .orElse(null);
        String rank = LexoRank.between(lastRank, null);

        ItemList itemList = new ItemList(request.title(), category, false, rank, user);

        itemListRepository.save(itemList);

        return itemListMapper.toDto(itemList);
    }

    public void delete(User user, Long id) {
        ItemList itemList = find(user, id);

        itemListRepository.delete(itemList);
    }

    public ItemListDto update(User user, Long id, UpdateItemListRequest request) {
        ItemList itemList = find(user, id);

        itemListMapper.update(itemList, request);
        if (request.category() != null) {
            itemList.setCategory(categoryService.find(user, request.category()));
        }

        itemListRepository.save(itemList);

        return itemListMapper.toDto(itemList);
    }

    public ItemListDto reorder(User user, Long id, ReorderItemListRequest request) {
        ItemList itemList = find(user, id);

        String prevRank = neighborRank(user, itemList, request.previousId());
        String nextRank = neighborRank(user, itemList, request.nextId());

        itemList.setRank(LexoRank.between(prevRank, nextRank));
        itemListRepository.save(itemList);

        return itemListMapper.toDto(itemList);
    }

    private String neighborRank(User user, ItemList target, Long neighborId) {
        if (neighborId == null) return null;
        ItemList neighbor = find(user, neighborId);
        if (!neighbor.getCategory().getId().equals(target.getCategory().getId())) {
            throw new BadRequestException("Neighbor must be in the same category.");
        }
        return neighbor.getRank();
    }
}
