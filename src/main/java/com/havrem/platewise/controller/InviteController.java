package com.havrem.platewise.controller;

import com.havrem.platewise.config.CurrentUser;
import com.havrem.platewise.dto.invite.CreateInviteRequest;
import com.havrem.platewise.dto.invite.InviteDto;
import com.havrem.platewise.entity.User;
import com.havrem.platewise.service.ListInviteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/invites")
public class InviteController {
    private final ListInviteService inviteService;

    public InviteController(ListInviteService inviteService) {
        this.inviteService = inviteService;
    }

    @GetMapping
    public List<InviteDto> myInvites(@CurrentUser User user) {
        return inviteService.myInvites(user);
    }

    @PostMapping("/{id}/accept")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void accept(@CurrentUser User user, @PathVariable Long id) {
        inviteService.accept(user, id);
    }

    @PostMapping("/{id}/decline")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void decline(@CurrentUser User user, @PathVariable Long id) {
        inviteService.decline(user, id);
    }
}
