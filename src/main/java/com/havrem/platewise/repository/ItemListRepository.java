package com.havrem.platewise.repository;

import com.havrem.platewise.entity.ItemList;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItemListRepository extends JpaRepository<ItemList, Long> {
    @EntityGraph(attributePaths = {"category", "items"})
    List<ItemList> findAllByUserIdOrderByRankAsc(Long userId);

    @EntityGraph(attributePaths = {"category", "items"})
    Optional<ItemList> findByUserIdAndId(Long userId, Long id);

    Optional<ItemList> findFirstByUserIdAndCategoryIdOrderByRankDesc(Long userId, Long categoryId);
}
