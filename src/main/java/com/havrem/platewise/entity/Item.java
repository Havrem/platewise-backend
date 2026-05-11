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
}
