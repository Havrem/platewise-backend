package com.havrem.platewise.service;

import com.havrem.platewise.dto.item.CreateItemRequest;
import com.havrem.platewise.dto.item.ItemDto;
import com.havrem.platewise.dto.item.ReorderItemRequest;
import com.havrem.platewise.dto.item.UpdateItemRequest;
import com.havrem.platewise.entity.Item;
import com.havrem.platewise.entity.ItemList;
import com.havrem.platewise.entity.User;
import com.havrem.platewise.exception.BadRequestException;
import com.havrem.platewise.exception.NotFoundException;
import com.havrem.platewise.mapper.ItemMapper;
import com.havrem.platewise.repository.ItemRepository;
import com.havrem.platewise.util.LexoRank;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemService {
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final ItemListService itemListService;

    public ItemService(ItemRepository itemRepository, ItemMapper itemMapper, ItemListService itemListService) {
        this.itemRepository = itemRepository;
        this.itemMapper = itemMapper;
        this.itemListService = itemListService;
    }

    public Item find(User user, Long id) {
        return itemRepository.findByUserIdAndId(user.getId(), id).orElseThrow(() -> NotFoundException.of("Item", id));
    }

    public ItemDto create(User user, CreateItemRequest request) {
        ItemList itemList = itemListService.find(user, request.itemListId());

        String lastRank = itemRepository
                .findFirstByUserIdAndItemListIdOrderByRankDesc(user.getId(), itemList.getId())
                .map(Item::getRank)
                .orElse(null);
        String rank = LexoRank.between(lastRank, null);

        Item item = new Item(request.text(), request.completed(), itemList, user, request.type(), rank);

        itemRepository.save(item);

        return itemMapper.toDto(item);
    }

    public ItemDto read(User user, Long id) {
        Item item = find(user, id);

        return itemMapper.toDto(item);
    }

    public List<ItemDto> readAll(User user) {
        List<Item> items = itemRepository.findAllByUserIdOrderByRankAsc(user.getId());

        return itemMapper.toDtos(items);
    }

    public ItemDto update(User user, Long id, UpdateItemRequest request) {
        Item item = find(user, id);

        itemMapper.update(item, request);

        itemRepository.save(item);

        return itemMapper.toDto(item);
    }

    public void delete(User user, Long id) {
        Item item = find(user, id);

        itemRepository.delete(item);
    }

    public ItemDto reorder(User user, Long id, ReorderItemRequest request) {
        Item item = find(user, id);

        String prevRank = neighborRank(user, item, request.previousId());
        String nextRank = neighborRank(user, item, request.nextId());

        item.setRank(LexoRank.between(prevRank, nextRank));
        itemRepository.save(item);

        return itemMapper.toDto(item);
    }

    private String neighborRank(User user, Item target, Long neighborId) {
        if (neighborId == null) return null;
        Item neighbor = find(user, neighborId);
        if (!neighbor.getItemList().getId().equals(target.getItemList().getId())) {
            throw new BadRequestException("Neighbor must be in the same item list.");
        }
        return neighbor.getRank();
    }
}
