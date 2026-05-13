package com.havrem.platewise.dto.response;

public record AuthResponse(String accessToken, Long userId, String email) {
}
