package com.havrem.platewise.repository;

import com.havrem.platewise.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}