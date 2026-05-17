package com.havrem.platewise.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "list_sections")
public class ListSection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_list_id")
    private ItemList itemList;

    private String text;

    private String rank;

    protected ListSection() {
    }

    public ListSection(ItemList itemList, String text, String rank) {
        this.itemList = itemList;
        this.text = text;
        this.rank = rank;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ItemList getItemList() {
        return itemList;
    }

    public void setItemList(ItemList itemList) {
        this.itemList = itemList;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }
}
