package com.havrem.platewise.dto.invite;

public record InviteDto(
        Long id,
        Long listId,
        String listTitle,
        String inviterEmail
) {
}
