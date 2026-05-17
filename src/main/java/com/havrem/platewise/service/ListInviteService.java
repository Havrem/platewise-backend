package com.havrem.platewise.service;

import com.havrem.platewise.dto.invite.CreateInviteRequest;
import com.havrem.platewise.dto.invite.InviteDto;
import com.havrem.platewise.entity.ItemList;
import com.havrem.platewise.entity.ListInvite;
import com.havrem.platewise.entity.ListMember;
import com.havrem.platewise.entity.User;
import com.havrem.platewise.exception.BadRequestException;
import com.havrem.platewise.exception.ConflictException;
import com.havrem.platewise.exception.NotFoundException;
import com.havrem.platewise.repository.ListInviteRepository;
import com.havrem.platewise.repository.ListMemberRepository;
import com.havrem.platewise.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListInviteService {
    private final ListInviteRepository inviteRepository;
    private final ListMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final ItemListService itemListService;
    private final RealtimeBroadcaster broadcaster;

    public ListInviteService(ListInviteRepository inviteRepository,
                             ListMemberRepository memberRepository,
                             UserRepository userRepository,
                             ItemListService itemListService,
                             RealtimeBroadcaster broadcaster) {
        this.inviteRepository = inviteRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
        this.itemListService = itemListService;
        this.broadcaster = broadcaster;
    }

    @Transactional
    public InviteDto invite(User inviter, Long listId, CreateInviteRequest request) {
        ItemList list = itemListService.find(inviter, listId);
        itemListService.requireOwner(list, inviter);

        User invitee = userRepository.findByEmail(request.email())
                .orElseThrow(() -> NotFoundException.of("User", request.email()));

        if (invitee.getId().equals(inviter.getId())) {
            throw new BadRequestException("You cannot invite yourself.");
        }
        if (memberRepository.existsByListIdAndUserId(listId, invitee.getId())) {
            throw new ConflictException("User is already a member of this list.");
        }
        if (inviteRepository.existsByListIdAndInviteeId(listId, invitee.getId())) {
            throw new ConflictException("User has already been invited.");
        }

        ListInvite invite = inviteRepository.save(new ListInvite(list, inviter, invitee));
        broadcaster.inviteCreated(invitee.getEmail(), invite.getId());
        return toDto(invite);
    }

    public List<InviteDto> myInvites(User user) {
        return inviteRepository.findAllByInviteeIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public void accept(User user, Long inviteId) {
        ListInvite invite = inviteRepository.findByIdAndInviteeId(inviteId, user.getId())
                .orElseThrow(() -> NotFoundException.of("Invite", inviteId));
        memberRepository.save(new ListMember(invite.getList(), user, false));
        Long listId = invite.getList().getId();
        inviteRepository.delete(invite);
        broadcaster.listChanged(listId);
    }

    @Transactional
    public void decline(User user, Long inviteId) {
        ListInvite invite = inviteRepository.findByIdAndInviteeId(inviteId, user.getId())
                .orElseThrow(() -> NotFoundException.of("Invite", inviteId));
        inviteRepository.delete(invite);
    }

    private InviteDto toDto(ListInvite invite) {
        return new InviteDto(
                invite.getId(),
                invite.getList().getId(),
                invite.getList().getTitle(),
                invite.getInviter().getEmail()
        );
    }
}
