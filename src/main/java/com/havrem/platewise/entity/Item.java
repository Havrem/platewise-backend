package com.havrem.platewise.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "items")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long Id;

    String text;
    Boolean isDone;

    public enum Type {
        BULLET, CHECK, NUMBERED, NONE
    }

    public Item() {
    }

    public Item(String text, Boolean isDone) {
        this.text = text;
        this.isDone = isDone;
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

    public Boolean getDone() {
        return isDone;
    }

    public void setDone(Boolean done) {
        isDone = done;
    }
}
