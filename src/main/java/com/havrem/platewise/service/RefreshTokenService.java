package com.havrem.platewise.service;

import com.havrem.platewise.config.JwtProperties;
import com.havrem.platewise.entity.RefreshToken;
import com.havrem.platewise.entity.User;
import com.havrem.platewise.exception.UnauthorizedException;
import com.havrem.platewise.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;

@Service
public class RefreshTokenService {
    private static final int TOKEN_BYTES = 32;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final RefreshTokenRepository repository;
    private final long ttlSeconds;

    public RefreshTokenService(RefreshTokenRepository repository, JwtProperties properties) {
        this.repository = repository;
        this.ttlSeconds = properties.refreshTtlSeconds();
    }

    public record Rotated(User user, String rawToken) {
    }

    @Transactional
    public String create(User user) {
        String raw = randomToken();
        RefreshToken token = new RefreshToken(hash(raw), user, Instant.now().plusSeconds(ttlSeconds));
        repository.save(token);
        return raw;
    }

    @Transactional
    public Rotated validateAndRotate(String rawToken) {
        RefreshToken existing = repository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token."));

        if (existing.getExpiresAt().isBefore(Instant.now())) {
            repository.delete(existing);
            throw new UnauthorizedException("Refresh token expired.");
        }

        String newRaw = randomToken();
        existing.setTokenHash(hash(newRaw));
        existing.setExpiresAt(Instant.now().plusSeconds(ttlSeconds));
        repository.save(existing);
        return new Rotated(existing.getUser(), newRaw);
    }

    @Transactional
    public void delete(String rawToken) {
        repository.findByTokenHash(hash(rawToken)).ifPresent(repository::delete);
    }

    private static String randomToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private static String hash(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] out = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(out);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
