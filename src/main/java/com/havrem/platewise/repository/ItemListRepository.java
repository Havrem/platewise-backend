package com.havrem.platewise.repository;

import com.havrem.platewise.entity.ItemList;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItemListRepository extends JpaRepository<ItemList, Long> {
    @EntityGraph(attributePaths = "category")
    List<ItemList> findAllByCategoryUserId(Long userId);

    @EntityGraph(attributePaths = "category")
    @Override
    Optional<ItemList> findById(Long id);
}