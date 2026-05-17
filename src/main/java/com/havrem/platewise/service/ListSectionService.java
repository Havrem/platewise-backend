package com.havrem.platewise.service;

import com.havrem.platewise.dto.listSection.CreateListSectionRequest;
import com.havrem.platewise.dto.listSection.ListSectionDto;
import com.havrem.platewise.dto.listSection.ReorderListSectionRequest;
import com.havrem.platewise.dto.listSection.UpdateListSectionRequest;
import com.havrem.platewise.entity.Item;
import com.havrem.platewise.entity.ItemList;
import com.havrem.platewise.entity.ListSection;
import com.havrem.platewise.entity.User;
import com.havrem.platewise.exception.BadRequestException;
import com.havrem.platewise.exception.NotFoundException;
import com.havrem.platewise.mapper.ListSectionMapper;
import com.havrem.platewise.repository.ItemRepository;
import com.havrem.platewise.repository.ListSectionRepository;
import com.havrem.platewise.util.LexoRank;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListSectionService {
    private final ListSectionRepository sectionRepository;
    private final ItemRepository itemRepository;
    private final ItemListService itemListService;
    private final ListSectionMapper sectionMapper;
    private final RealtimeBroadcaster broadcaster;

    public ListSectionService(ListSectionRepository sectionRepository,
                              ItemRepository itemRepository,
                              ItemListService itemListService,
                              ListSectionMapper sectionMapper,
                              RealtimeBroadcaster broadcaster) {
        this.sectionRepository = sectionRepository;
        this.itemRepository = itemRepository;
        this.itemListService = itemListService;
        this.sectionMapper = sectionMapper;
        this.broadcaster = broadcaster;
    }

    public ListSection find(User user, Long id) {
        return sectionRepository.findByIdForMember(id, user.getId())
                .orElseThrow(() -> NotFoundException.of("ListSection", id));
    }

    @Transactional
    public ListSectionDto create(User user, Long listId, CreateListSectionRequest request) {
        ItemList list = itemListService.find(user, listId);
        String lastRank = sectionRepository
                .findFirstByItemListIdOrderByRankDesc(list.getId())
                .map(ListSection::getRank)
                .orElse(null);

        ListSection section = new ListSection(list, request.text(), LexoRank.between(lastRank, null));
        sectionRepository.save(section);
        list.getSections().add(section);
        broadcaster.listChanged(list.getId());

        return sectionMapper.toDto(section);
    }

    @Transactional
    public ListSectionDto update(User user, Long id, UpdateListSectionRequest request) {
        ListSection section = find(user, id);
        sectionMapper.update(section, request);
        sectionRepository.save(section);
        broadcaster.listChanged(section.getItemList().getId());

        return sectionMapper.toDto(section);
    }

    @Transactional
    public ListSectionDto reorder(User user, Long id, ReorderListSectionRequest request) {
        ListSection section = find(user, id);
        String prevRank = neighborRank(user, section, request.previousId());
        String nextRank = neighborRank(user, section, request.nextId());

        section.setRank(LexoRank.between(prevRank, nextRank));
        sectionRepository.save(section);
        broadcaster.listChanged(section.getItemList().getId());

        return sectionMapper.toDto(section);
    }

    @Transactional
    public void delete(User user, Long id) {
        ListSection section = find(user, id);
        Long listId = section.getItemList().getId();
        List<Item> items = itemRepository.findAllBySectionId(section.getId());
        items.forEach(item -> item.setSection(null));
        itemRepository.saveAll(items);
        sectionRepository.delete(section);
        broadcaster.listChanged(listId);
    }

    private String neighborRank(User user, ListSection target, Long neighborId) {
        if (neighborId == null) return null;
        ListSection neighbor = find(user, neighborId);
        if (!neighbor.getItemList().getId().equals(target.getItemList().getId())) {
            throw new BadRequestException("Neighbor must be in the same item list.");
        }
        return neighbor.getRank();
    }
}
