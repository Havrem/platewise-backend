package com.havrem.platewise.repository;

import com.havrem.platewise.entity.ListSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ListSectionRepository extends JpaRepository<ListSection, Long> {
    @Query("select s from ListSection s join ListMember m on m.list = s.itemList where s.id = :id and m.user.id = :userId")
    Optional<ListSection> findByIdForMember(@Param("id") Long id, @Param("userId") Long userId);

    Optional<ListSection> findFirstByItemListIdOrderByRankDesc(Long itemListId);

    List<ListSection> findAllByItemListIdOrderByRankAsc(Long itemListId);
}
