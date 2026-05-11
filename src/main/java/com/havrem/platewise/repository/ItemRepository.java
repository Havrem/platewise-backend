package com.havrem.platewise.repository;

import com.havrem.platewise.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
}