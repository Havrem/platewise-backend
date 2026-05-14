package com.havrem.platewise.dto.auth;

public record AuthResponse(String accessToken, Long userId, String email) {
}
