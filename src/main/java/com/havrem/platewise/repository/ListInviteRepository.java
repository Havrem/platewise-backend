package com.havrem.platewise.repository;

import com.havrem.platewise.entity.ListInvite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ListInviteRepository extends JpaRepository<ListInvite, Long> {
    List<ListInvite> findAllByInviteeIdOrderByCreatedAtDesc(Long inviteeId);
    Optional<ListInvite> findByIdAndInviteeId(Long id, Long inviteeId);
    boolean existsByListIdAndInviteeId(Long listId, Long inviteeId);
}
