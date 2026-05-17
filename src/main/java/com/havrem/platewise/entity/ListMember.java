package com.havrem.platewise.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "list_members")
public class ListMember {
    @EmbeddedId
    private ListMemberId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("listId")
    @JoinColumn(name = "list_id")
    private ItemList list;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "is_owner")
    private boolean owner;

    @Column(name = "joined_at")
    private Instant joinedAt;

    protected ListMember() {}

    public ListMember(ItemList list, User user, boolean owner) {
        this.id = new ListMemberId(list.getId(), user.getId());
        this.list = list;
        this.user = user;
        this.owner = owner;
        this.joinedAt = Instant.now();
    }

    public ListMemberId getId() { return id; }
    public ItemList getList() { return list; }
    public User getUser() { return user; }
    public boolean isOwner() { return owner; }
    public Instant getJoinedAt() { return joinedAt; }
}
