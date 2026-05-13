package com.havrem.platewise.service;

import com.havrem.platewise.dto.request.LoginRequest;
import com.havrem.platewise.dto.request.SignupRequest;
import com.havrem.platewise.dto.response.AuthResponse;
import com.havrem.platewise.entity.User;
import com.havrem.platewise.exception.ConflictException;
import com.havrem.platewise.exception.UnauthorizedException;
import com.havrem.platewise.mapper.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already in use.");
        }

        User user = new User(request.email(), passwordEncoder.encode(request.password()));
        User saved = userRepository.save(user);

        String token = jwtService.generate(saved.getId());

        return new AuthResponse(token, saved.getId(), saved.getEmail());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email()).orElseThrow(() -> new UnauthorizedException("Invalid email or password."));

        if(!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password.");
        }

        String token = jwtService.generate(user.getId());

        return new AuthResponse(token, user.getId(), user.getEmail());
    }
}
