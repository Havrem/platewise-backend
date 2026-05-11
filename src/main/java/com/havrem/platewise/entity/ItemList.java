package com.havrem.platewise.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class ItemList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long Id;

    String title;

    @ManyToOne
    @JoinColumn(name = "category_id")
    Category category;

    Boolean bookmarked;

    @OneToMany
    List<Item> items;
}
