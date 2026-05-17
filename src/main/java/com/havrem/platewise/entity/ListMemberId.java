package com.havrem.platewise.entity;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ListMemberId implements Serializable {
    private Long listId;
    private Long userId;

    public ListMemberId() {}

    public ListMemberId(Long listId, Long userId) {
        this.listId = listId;
        this.userId = userId;
    }

    public Long getListId() { return listId; }
    public Long getUserId() { return userId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ListMemberId other)) return false;
        return Objects.equals(listId, other.listId) && Objects.equals(userId, other.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(listId, userId);
    }
}
