package com.havrem.platewise.dto.auth;

public record AuthResponse(String accessToken, String refreshToken, Long userId, String email) {
}
