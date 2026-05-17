package com.havrem.platewise.service;

import com.havrem.platewise.dto.category.CategoryDto;
import com.havrem.platewise.dto.itemList.CreateItemListRequest;
import com.havrem.platewise.dto.itemList.ItemListDto;
import com.havrem.platewise.dto.itemList.ReorderItemListRequest;
import com.havrem.platewise.dto.itemList.UpdateItemListRequest;
import com.havrem.platewise.entity.Category;
import com.havrem.platewise.entity.ItemList;
import com.havrem.platewise.entity.ListMember;
import com.havrem.platewise.entity.User;
import com.havrem.platewise.exception.BadRequestException;
import com.havrem.platewise.exception.NotFoundException;
import com.havrem.platewise.mapper.CategoryMapper;
import com.havrem.platewise.mapper.ItemListMapper;
import com.havrem.platewise.repository.CategoryRepository;
import com.havrem.platewise.repository.ItemListRepository;
import com.havrem.platewise.repository.ListMemberRepository;
import com.havrem.platewise.util.LexoRank;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ItemListService {
    private final ItemListRepository itemListRepository;
    private final ItemListMapper itemListMapper;
    private final CategoryService categoryService;
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final ListMemberRepository listMemberRepository;
    private final RealtimeBroadcaster broadcaster;

    public ItemListService(ItemListRepository itemListRepository,
                           ItemListMapper itemListMapper,
                           CategoryService categoryService,
                           CategoryRepository categoryRepository,
                           CategoryMapper categoryMapper,
                           ListMemberRepository listMemberRepository,
                           RealtimeBroadcaster broadcaster) {
        this.itemListRepository = itemListRepository;
        this.itemListMapper = itemListMapper;
        this.categoryService = categoryService;
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
        this.listMemberRepository = listMemberRepository;
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
        return new ItemListDto(dto.id(), dto.title(), sharedDto, dto.type(), dto.bookmarked(), dto.items());
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
