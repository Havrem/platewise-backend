package com.havrem.platewise.repository;

import com.havrem.platewise.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {
    Optional<Item> findByUserIdAndId(Long userId, Long id);
    List<Item> findAllByUserIdOrderByRankAsc(Long userId);
    Optional<Item> findFirstByUserIdAndItemListIdOrderByRankDesc(Long userId, Long itemListId);
}
