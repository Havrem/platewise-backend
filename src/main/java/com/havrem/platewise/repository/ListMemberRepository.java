package com.havrem.platewise.repository;

import com.havrem.platewise.entity.ListMember;
import com.havrem.platewise.entity.ListMemberId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ListMemberRepository extends JpaRepository<ListMember, ListMemberId> {
    boolean existsByListIdAndUserId(Long listId, Long userId);
    Optional<ListMember> findByListIdAndUserId(Long listId, Long userId);
    List<ListMember> findAllByListId(Long listId);
    void deleteByListIdAndUserId(Long listId, Long userId);
}
