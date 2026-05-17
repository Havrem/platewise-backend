package com.havrem.platewise.service;

import com.havrem.platewise.entity.ListMember;
import com.havrem.platewise.repository.ListMemberRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RealtimeBroadcaster {
    private final SimpMessagingTemplate template;
    private final ListMemberRepository memberRepository;

    public RealtimeBroadcaster(SimpMessagingTemplate template,
                               ListMemberRepository memberRepository) {
        this.template = template;
        this.memberRepository = memberRepository;
    }

    /** Tell every member of this list that something inside it changed. */
    public void listChanged(Long listId) {
        Map<String, Object> payload = Map.of("type", "list-changed", "listId", listId);
        for (ListMember member : memberRepository.findAllByListId(listId)) {
            String email = member.getUser().getEmail();
            template.convertAndSendToUser(email, "/queue/lists/" + listId, payload);
        }
    }

    /** Tell a specific user they have a new invite. */
    public void inviteCreated(String email, Long inviteId) {
        Map<String, Object> payload = Map.of("type", "invite-created", "inviteId", inviteId);
        template.convertAndSendToUser(email, "/queue/invites", payload);
    }
}
