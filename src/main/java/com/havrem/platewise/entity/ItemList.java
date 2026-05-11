package com.havrem.platewise.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
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

    public ItemList() {
    }

    public ItemList(String title, Category category, Boolean bookmarked) {
        this.items = new ArrayList<>();

        this.title = title;
        this.category = category;
        this.bookmarked = bookmarked;
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Boolean getBookmarked() {
        return bookmarked;
    }

    public void setBookmarked(Boolean bookmarked) {
        this.bookmarked = bookmarked;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }
}
