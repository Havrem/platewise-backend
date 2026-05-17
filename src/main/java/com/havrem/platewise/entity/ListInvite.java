package com.havrem.platewise.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "list_invites")
public class ListInvite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "list_id")
    private ItemList list;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inviter_id")
    private User inviter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invitee_id")
    private User invitee;

    @Column(name = "created_at")
    private Instant createdAt;

    protected ListInvite() {}

    public ListInvite(ItemList list, User inviter, User invitee) {
        this.list = list;
        this.inviter = inviter;
        this.invitee = invitee;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public ItemList getList() { return list; }
    public User getInviter() { return inviter; }
    public User getInvitee() { return invitee; }
    public Instant getCreatedAt() { return createdAt; }
}
