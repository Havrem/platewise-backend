package com.havrem.platewise.repository;

import com.havrem.platewise.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByUserIdAndId(Long userId, Long id);

    List<Category> findAllByUserId(Long userId);
}