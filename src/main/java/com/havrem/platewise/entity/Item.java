package com.havrem.platewise.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "items")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    private String text;
    private Boolean completed;

    @ManyToOne
    @JoinColumn(name = "item_list_id")
    private ItemList itemList;

    public ItemList getItemList() {
        return itemList;
    }

    public void setItemList(ItemList itemList) {
        this.itemList = itemList;
    }

    public enum Type {
        BULLET, CHECK, NUMBERED, NONE
    }

    protected Item() {
    }

    public Item(String text, Boolean completed) {
        this.text = text;
        this.completed = completed;
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
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
}
