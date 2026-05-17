package com.havrem.platewise.service;

import com.havrem.platewise.dto.item.CreateItemRequest;
import com.havrem.platewise.dto.item.ItemDto;
import com.havrem.platewise.dto.item.ReorderItemRequest;
import com.havrem.platewise.dto.item.UpdateItemRequest;
import com.havrem.platewise.entity.Item;
import com.havrem.platewise.entity.ItemList;
import com.havrem.platewise.entity.ListSection;
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
    private final ListSectionService sectionService;
    private final RealtimeBroadcaster broadcaster;

    public ItemService(ItemRepository itemRepository,
                       ItemMapper itemMapper,
                       ItemListService itemListService,
                       ListSectionService sectionService,
                       RealtimeBroadcaster broadcaster) {
        this.itemRepository = itemRepository;
        this.itemMapper = itemMapper;
        this.itemListService = itemListService;
        this.sectionService = sectionService;
        this.broadcaster = broadcaster;
    }

    public Item find(User user, Long id) {
        return itemRepository.findByIdForMember(id, user.getId()).orElseThrow(() -> NotFoundException.of("Item", id));
    }

    public ItemDto create(User user, CreateItemRequest request) {
        ItemList itemList = itemListService.find(user, request.itemListId());
        ListSection section = resolveSection(user, itemList, request.sectionId());

        String lastRank = itemRepository
                .findFirstByItemListIdOrderByRankDesc(itemList.getId())
                .map(Item::getRank)
                .orElse(null);
        String rank = LexoRank.between(lastRank, null);

        Item item = new Item(request.text(), request.completed(), itemList, section, user, request.type(), rank);

        itemRepository.save(item);
        broadcaster.listChanged(itemList.getId());

        return itemMapper.toDto(item);
    }

    public ItemDto read(User user, Long id) {
        Item item = find(user, id);

        return itemMapper.toDto(item);
    }

    public List<ItemDto> readAll(User user) {
        List<Item> items = itemRepository.findAllForMember(user.getId());

        return itemMapper.toDtos(items);
    }

    public ItemDto update(User user, Long id, UpdateItemRequest request) {
        Item item = find(user, id);

        itemMapper.update(item, request);

        itemRepository.save(item);
        broadcaster.listChanged(item.getItemList().getId());

        return itemMapper.toDto(item);
    }

    public void delete(User user, Long id) {
        Item item = find(user, id);
        Long listId = item.getItemList().getId();

        itemRepository.delete(item);
        broadcaster.listChanged(listId);
    }

    public ItemDto reorder(User user, Long id, ReorderItemRequest request) {
        Item item = find(user, id);
        ListSection section = resolveSection(user, item.getItemList(), request.sectionId());

        String prevRank = neighborRank(user, item, section, request.previousId());
        String nextRank = neighborRank(user, item, section, request.nextId());

        item.setSection(section);
        item.setRank(LexoRank.between(prevRank, nextRank));
        itemRepository.save(item);
        broadcaster.listChanged(item.getItemList().getId());

        return itemMapper.toDto(item);
    }

    private ListSection resolveSection(User user, ItemList itemList, Long sectionId) {
        if (sectionId == null) return null;
        ListSection section = sectionService.find(user, sectionId);
        if (!section.getItemList().getId().equals(itemList.getId())) {
            throw new BadRequestException("Section must be in the same item list.");
        }
        return section;
    }

    private String neighborRank(User user, Item target, ListSection section, Long neighborId) {
        if (neighborId == null) return null;
        Item neighbor = find(user, neighborId);
        if (!neighbor.getItemList().getId().equals(target.getItemList().getId())) {
            throw new BadRequestException("Neighbor must be in the same item list.");
        }
        Long sectionId = section == null ? null : section.getId();
        Long neighborSectionId = neighbor.getSection() == null ? null : neighbor.getSection().getId();
        if (!java.util.Objects.equals(sectionId, neighborSectionId)) {
            throw new BadRequestException("Neighbor must be in the same section.");
        }
        return neighbor.getRank();
    }
}
