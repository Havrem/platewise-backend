package com.havrem.platewise.repository;

import com.havrem.platewise.entity.ItemList;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemListRepository extends JpaRepository<ItemList, Long> {
}