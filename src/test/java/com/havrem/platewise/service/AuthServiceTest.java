package com.havrem.platewise.service;

import com.havrem.platewise.dto.auth.AuthResponse;
import com.havrem.platewise.dto.auth.LoginRequest;
import com.havrem.platewise.dto.auth.SignupRequest;
import com.havrem.platewise.entity.User;
import com.havrem.platewise.exception.ConflictException;
import com.havrem.platewise.exception.UnauthorizedException;
import com.havrem.platewise.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private AuthService authService;

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtService = mock(JwtService.class);
        authService = new AuthService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void signup_existingEmail_throwsConflictException() {
        when(userRepository.existsByEmail("a@b.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.signup(new SignupRequest("a@b.com", "password123")))
                .isInstanceOf(ConflictException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void signup_newUser_hashesPasswordBeforePersisting() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
        when(userRepository.save(any())).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(42L);
            return u;
        });
        when(jwtService.generate(42L)).thenReturn("token-abc");

        AuthResponse response = authService.signup(new SignupRequest("a@b.com", "password123"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("a@b.com");
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("hashed-password");

        assertThat(response.accessToken()).isEqualTo("token-abc");
        assertThat(response.userId()).isEqualTo(42L);
        assertThat(response.email()).isEqualTo("a@b.com");
    }

    @Test
    void login_unknownEmail_throwsUnauthorized() {
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("a@b.com", "password123")))
                .isInstanceOf(UnauthorizedException.class);

        verify(jwtService, never()).generate(any());
    }

    @Test
    void login_wrongPassword_throwsUnauthorized() {
        User user = new User("a@b.com", "hashed-password");
        user.setId(1L);
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("a@b.com", "wrong")))
                .isInstanceOf(UnauthorizedException.class);

        verify(jwtService, never()).generate(any());
    }

    @Test
    void login_validCredentials_returnsTokenForUser() {
        User user = new User("a@b.com", "hashed-password");
        user.setId(42L);
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed-password")).thenReturn(true);
        when(jwtService.generate(42L)).thenReturn("token-xyz");

        AuthResponse response = authService.login(new LoginRequest("a@b.com", "password123"));

        assertThat(response.accessToken()).isEqualTo("token-xyz");
        assertThat(response.userId()).isEqualTo(42L);
        assertThat(response.email()).isEqualTo("a@b.com");
        verify(jwtService).generate(eq(42L));
    }
}
