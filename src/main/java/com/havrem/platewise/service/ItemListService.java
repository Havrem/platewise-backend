package com.havrem.platewise.service;

import com.havrem.platewise.dto.category.CategoryDto;
import com.havrem.platewise.dto.itemList.CreateItemListRequest;
import com.havrem.platewise.dto.itemList.ImportItemsRequest;
import com.havrem.platewise.dto.itemList.ItemListDto;
import com.havrem.platewise.dto.itemList.ReorderItemListRequest;
import com.havrem.platewise.dto.itemList.UpdateItemListRequest;
import com.havrem.platewise.entity.Category;
import com.havrem.platewise.entity.Item;
import com.havrem.platewise.entity.ItemList;
import com.havrem.platewise.entity.ListMember;
import com.havrem.platewise.entity.ListSection;
import com.havrem.platewise.entity.User;
import com.havrem.platewise.exception.BadRequestException;
import com.havrem.platewise.exception.ExternalServiceException;
import com.havrem.platewise.exception.NotFoundException;
import com.havrem.platewise.mapper.CategoryMapper;
import com.havrem.platewise.mapper.ItemListMapper;
import com.havrem.platewise.repository.CategoryRepository;
import com.havrem.platewise.repository.ItemRepository;
import com.havrem.platewise.repository.ItemListRepository;
import com.havrem.platewise.repository.ListMemberRepository;
import com.havrem.platewise.repository.ListSectionRepository;
import com.havrem.platewise.util.LexoRank;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ItemListService {
    private final ItemListRepository itemListRepository;
    private final ItemListMapper itemListMapper;
    private final ItemRepository itemRepository;
    private final CategoryService categoryService;
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final ListMemberRepository listMemberRepository;
    private final ListSectionRepository sectionRepository;
    private final GroceryOrganizer groceryOrganizer;
    private final RealtimeBroadcaster broadcaster;

    public ItemListService(ItemListRepository itemListRepository,
                           ItemListMapper itemListMapper,
                           ItemRepository itemRepository,
                           CategoryService categoryService,
                           CategoryRepository categoryRepository,
                           CategoryMapper categoryMapper,
                           ListMemberRepository listMemberRepository,
                           ListSectionRepository sectionRepository,
                           GroceryOrganizer groceryOrganizer,
                           RealtimeBroadcaster broadcaster) {
        this.itemListRepository = itemListRepository;
        this.itemListMapper = itemListMapper;
        this.itemRepository = itemRepository;
        this.categoryService = categoryService;
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
        this.listMemberRepository = listMemberRepository;
        this.sectionRepository = sectionRepository;
        this.groceryOrganizer = groceryOrganizer;
        this.broadcaster = broadcaster;
    }

    public ItemList find(User user, Long id) {
        return itemListRepository.findByIdForMember(id, user.getId())
                .orElseThrow(() -> NotFoundException.of("ItemList", id));
    }

    public ItemListDto read(User user, Long id) {
        ItemList list = find(user, id);
        return toDtoForUser(list, user);
    }

    public List<ItemListDto> readAll(User user) {
        return itemListRepository.findAllForMember(user.getId()).stream()
                .map(list -> toDtoForUser(list, user))
                .toList();
    }

    @Transactional
    public ItemListDto create(User user, CreateItemListRequest request) {
        Category category = categoryService.find(user, request.category());
        if (category.getKind() == Category.Kind.SHARED) {
            throw new BadRequestException("Cannot create lists in the Shared category.");
        }

        String lastRank = itemListRepository
                .findFirstByMemberAndCategoryOrderByRankDesc(user.getId(), category.getId())
                .map(ItemList::getRank)
                .orElse(null);
        String rank = LexoRank.between(lastRank, null);

        ItemList itemList = new ItemList(request.title(), category, request.type(), false, rank);
        itemListRepository.save(itemList);
        listMemberRepository.save(new ListMember(itemList, user, true));

        return toDtoForUser(itemList, user);
    }

    @Transactional
    public ItemListDto importItems(User user, Long targetId, ImportItemsRequest request) {
        ItemList target = find(user, targetId);
        ItemList source = find(user, request.sourceListId());

        if (target.getId().equals(source.getId())) {
            throw new BadRequestException("Source list must be different from target list.");
        }

        List<Item> sourceItems = itemRepository.findAllByItemListIdOrderByRankAsc(source.getId());
        String lastRank = itemRepository
                .findFirstByItemListIdOrderByRankDesc(target.getId())
                .map(Item::getRank)
                .orElse(null);

        List<Item> targetItems = target.getItems();
        for (Item sourceItem : sourceItems) {
            lastRank = LexoRank.between(lastRank, null);
            Item copy = new Item(
                    sourceItem.getText(),
                    sourceItem.getCompleted(),
                    target,
                    user,
                    sourceItem.getType(),
                    lastRank
            );
            itemRepository.save(copy);
            targetItems.add(copy);
        }

        if (!sourceItems.isEmpty()) {
            broadcaster.listChanged(target.getId());
        }

        return toDtoForUser(target, user);
    }

    @Transactional
    public ItemListDto organizeGrocerySections(User user, Long id) {
        ItemList list = find(user, id);
        if (list.getType() != ItemList.Type.GROCERY) {
            throw new BadRequestException("Only grocery lists can be organized by food category.");
        }

        List<Item> items = itemRepository.findAllByItemListIdOrderByRankAsc(list.getId());
        if (items.isEmpty()) {
            return toDtoForUser(list, user);
        }

        List<ListSection> existingSections = sectionRepository.findAllByItemListIdOrderByRankAsc(list.getId());
        GroceryOrganization organization = groceryOrganizer.organize(list, items, existingSections);
        List<GroceryOrganization.Section> sections = validateOrganization(organization, items);

        replaceSections(list, items, existingSections, sections);
        broadcaster.listChanged(list.getId());

        return toDtoForUser(list, user);
    }

    @Transactional
    public void delete(User user, Long id) {
        ItemList itemList = find(user, id);
        requireOwner(itemList, user);
        itemListRepository.delete(itemList);
    }

    @Transactional
    public ItemListDto update(User user, Long id, UpdateItemListRequest request) {
        ItemList itemList = find(user, id);

        itemListMapper.update(itemList, request);
        if (request.category() != null) {
            itemList.setCategory(categoryService.find(user, request.category()));
        }

        itemListRepository.save(itemList);
        broadcaster.listChanged(itemList.getId());
        return toDtoForUser(itemList, user);
    }

    @Transactional
    public ItemListDto reorder(User user, Long id, ReorderItemListRequest request) {
        ItemList itemList = find(user, id);

        String prevRank = neighborRank(user, itemList, request.previousId());
        String nextRank = neighborRank(user, itemList, request.nextId());

        itemList.setRank(LexoRank.between(prevRank, nextRank));
        itemListRepository.save(itemList);
        broadcaster.listChanged(itemList.getId());

        return toDtoForUser(itemList, user);
    }

    public boolean isOwner(ItemList list, User user) {
        return listMemberRepository.findByListIdAndUserId(list.getId(), user.getId())
                .map(ListMember::isOwner)
                .orElse(false);
    }

    public void requireOwner(ItemList list, User user) {
        if (!isOwner(list, user)) {
            throw new BadRequestException("Only the owner can perform this action.");
        }
    }

    private ItemListDto toDtoForUser(ItemList list, User user) {
        ItemListDto dto = itemListMapper.toDto(list);
        if (isOwner(list, user)) return dto;
        Category sharedCategory = categoryRepository.findFirstByUserIdAndKind(user.getId(), Category.Kind.SHARED)
                .orElseGet(() -> categoryRepository.save(new Category("Shared", "shared", user, Category.Kind.SHARED)));
        CategoryDto sharedDto = categoryMapper.toDto(sharedCategory);
        return new ItemListDto(dto.id(), dto.title(), sharedDto, dto.type(), dto.bookmarked(), dto.sections(), dto.items());
    }

    private String neighborRank(User user, ItemList target, Long neighborId) {
        if (neighborId == null) return null;
        ItemList neighbor = find(user, neighborId);
        if (!neighbor.getCategory().getId().equals(target.getCategory().getId())) {
            throw new BadRequestException("Neighbor must be in the same category.");
        }
        return neighbor.getRank();
    }

    private List<GroceryOrganization.Section> validateOrganization(GroceryOrganization organization, List<Item> items) {
        if (organization == null || organization.sections() == null) {
            throw new ExternalServiceException("Gemini returned an invalid organization.");
        }

        Set<Long> expectedIds = new HashSet<>();
        for (Item item : items) {
            expectedIds.add(item.getId());
        }

        Set<Long> seenIds = new HashSet<>();
        List<GroceryOrganization.Section> validated = new ArrayList<>();
        for (GroceryOrganization.Section section : organization.sections()) {
            if (section == null || section.itemIds() == null) {
                throw new ExternalServiceException("Gemini returned an invalid organization.");
            }

            String text = section.text() == null ? "" : section.text().trim();
            if (text.isBlank() || text.length() > 500) {
                throw new ExternalServiceException("Gemini returned an invalid category.");
            }

            if (section.itemIds().isEmpty()) {
                continue;
            }

            List<Long> itemIds = new ArrayList<>();
            for (Long itemId : section.itemIds()) {
                if (itemId == null || !expectedIds.contains(itemId) || !seenIds.add(itemId)) {
                    throw new ExternalServiceException("Gemini returned an invalid item assignment.");
                }
                itemIds.add(itemId);
            }
            validated.add(new GroceryOrganization.Section(text, itemIds));
        }

        if (!seenIds.equals(expectedIds)) {
            throw new ExternalServiceException("Gemini did not organize every item.");
        }

        return validated;
    }

    private void replaceSections(ItemList list,
                                 List<Item> items,
                                 List<ListSection> existingSections,
                                 List<GroceryOrganization.Section> sections) {
        Map<Long, Item> itemsById = new HashMap<>();
        for (Item item : items) {
            item.setSection(null);
            itemsById.put(item.getId(), item);
        }
        itemRepository.saveAll(items);

        list.getSections().clear();
        sectionRepository.deleteAll(existingSections);

        String sectionRank = null;
        String itemRank = null;
        List<Item> organizedItems = new ArrayList<>();
        for (GroceryOrganization.Section plannedSection : sections) {
            sectionRank = LexoRank.between(sectionRank, null);
            ListSection section = new ListSection(list, plannedSection.text(), sectionRank);
            sectionRepository.save(section);
            list.getSections().add(section);

            for (Long itemId : plannedSection.itemIds()) {
                itemRank = LexoRank.between(itemRank, null);
                Item item = itemsById.get(itemId);
                item.setSection(section);
                item.setRank(itemRank);
                organizedItems.add(item);
            }
        }

        itemRepository.saveAll(organizedItems);
        list.getItems().sort(Comparator.comparing(Item::getRank));
    }
}
