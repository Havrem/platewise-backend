package com.havrem.platewise.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "item_lists")
public class ItemList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    public enum Type {
        GROCERY,
        RECIPES,
        GENERAL
    }

    @Enumerated(EnumType.STRING)
    private Type type;

    private boolean bookmarked;

    private String rank;

    @OneToMany(mappedBy = "list", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ListMember> members = new ArrayList<>();

    @OneToMany(mappedBy = "itemList", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("rank ASC")
    private List<ListSection> sections = new ArrayList<>();

    @OneToMany(mappedBy = "itemList", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("rank ASC")
    private List<Item> items = new ArrayList<>();

    protected ItemList() {
    }

    public ItemList(String title, Category category, Type type, boolean bookmarked, String rank) {
        this.title = title;
        this.category = category;
        this.type = type;
        this.bookmarked = bookmarked;
        this.rank = rank;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public boolean getBookmarked() {
        return bookmarked;
    }

    public void setBookmarked(boolean bookmarked) {
        this.bookmarked = bookmarked;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public List<ListSection> getSections() {
        return sections;
    }

    public void setSections(List<ListSection> sections) {
        this.sections = sections;
    }

    public boolean isBookmarked() {
        return bookmarked;
    }

    public List<ListMember> getMembers() {
        return members;
    }
}
