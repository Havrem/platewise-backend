package com.havrem.platewise.repository;

import com.havrem.platewise.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {
    @Query("select i from Item i join ListMember m on m.list = i.itemList where i.id = :id and m.user.id = :userId")
    Optional<Item> findByIdForMember(@Param("id") Long id, @Param("userId") Long userId);

    @Query("select i from Item i join ListMember m on m.list = i.itemList where m.user.id = :userId order by i.rank asc")
    List<Item> findAllForMember(@Param("userId") Long userId);

    Optional<Item> findFirstByItemListIdOrderByRankDesc(Long itemListId);

    List<Item> findAllByItemListIdOrderByRankAsc(Long itemListId);

    List<Item> findAllBySectionId(Long sectionId);
}
