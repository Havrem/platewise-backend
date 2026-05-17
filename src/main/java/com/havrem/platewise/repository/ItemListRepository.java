package com.havrem.platewise.repository;

import com.havrem.platewise.entity.ItemList;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItemListRepository extends JpaRepository<ItemList, Long> {
    @EntityGraph(attributePaths = {"category", "items"})
    @Query("select l from ItemList l join ListMember m on m.list = l where m.user.id = :userId order by l.rank asc")
    List<ItemList> findAllForMember(@Param("userId") Long userId);

    @EntityGraph(attributePaths = {"category", "items"})
    @Query("select l from ItemList l join ListMember m on m.list = l where m.user.id = :userId and l.id = :listId")
    Optional<ItemList> findByIdForMember(@Param("listId") Long listId, @Param("userId") Long userId);

    @Query("select l from ItemList l join ListMember m on m.list = l where m.user.id = :userId and l.category.id = :categoryId order by l.rank desc limit 1")
    Optional<ItemList> findFirstByMemberAndCategoryOrderByRankDesc(@Param("userId") Long userId, @Param("categoryId") Long categoryId);
}
