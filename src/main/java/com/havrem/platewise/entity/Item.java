package com.havrem.platewise.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "items")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;
    private Boolean completed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_list_id")
    private ItemList itemList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private ListSection section;

    public ItemList getItemList() {
        return itemList;
    }

    public void setItemList(ItemList itemList) {
        this.itemList = itemList;
    }

    public ListSection getSection() {
        return section;
    }

    public void setSection(ListSection section) {
        this.section = section;
    }

    public enum Type {
        BULLET, CHECKED, NUMBERED, NONE
    }

    @Enumerated(EnumType.STRING)
    private Type type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String rank;

    protected Item() {
    }

    public Item(String text, Boolean completed, ItemList itemList, User user, Type type, String rank) {
        this(text, completed, itemList, null, user, type, rank);
    }

    public Item(String text, Boolean completed, ItemList itemList, ListSection section, User user, Type type, String rank) {
        this.text = text;
        this.completed = completed;
        this.itemList = itemList;
        this.section = section;
        this.user = user;
        this.type = type;
        this.rank = rank;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }
}
