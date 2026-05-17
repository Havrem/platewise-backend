package com.havrem.platewise.service;

import com.havrem.platewise.dto.auth.AuthResponse;
import com.havrem.platewise.dto.auth.LoginRequest;
import com.havrem.platewise.dto.auth.SignupRequest;
import com.havrem.platewise.entity.Category;
import com.havrem.platewise.entity.User;
import com.havrem.platewise.exception.ConflictException;
import com.havrem.platewise.exception.UnauthorizedException;
import com.havrem.platewise.repository.CategoryRepository;
import com.havrem.platewise.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final CategoryRepository categoryRepository;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, RefreshTokenService refreshTokenService, CategoryRepository categoryRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already in use.");
        }

        User user = new User(request.email(), passwordEncoder.encode(request.password()));
        User saved = userRepository.save(user);

        categoryRepository.save(new Category("Shared", "shared", saved, Category.Kind.SHARED));

        return issueTokens(saved);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email()).orElseThrow(() -> new UnauthorizedException("Invalid email or password."));

        if(!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password.");
        }

        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refresh(String refreshToken) {
        RefreshTokenService.Rotated rotated = refreshTokenService.validateAndRotate(refreshToken);
        String accessToken = jwtService.generate(rotated.user().getId());
        return new AuthResponse(accessToken, rotated.rawToken(), rotated.user().getId(), rotated.user().getEmail());
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.delete(refreshToken);
    }

    private AuthResponse issueTokens(User user) {
        String accessToken = jwtService.generate(user.getId());
        String refreshToken = refreshTokenService.create(user);
        return new AuthResponse(accessToken, refreshToken, user.getId(), user.getEmail());
    }
}
