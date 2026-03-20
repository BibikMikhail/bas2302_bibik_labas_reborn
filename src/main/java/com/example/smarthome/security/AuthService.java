package com.example.smarthome.security;

import com.example.smarthome.repository.AppUserRepository;
import com.example.smarthome.repository.UserSessionRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(
            AppUserRepository appUserRepository,
            UserSessionRepository userSessionRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.appUserRepository = appUserRepository;
        this.userSessionRepository = userSessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public void registerUser(String username, String rawPassword) {
        registerWithRole(username, rawPassword, AppRole.USER);
    }

    @Transactional
    public void registerWithRole(String username, String rawPassword, AppRole role) {
        if (username == null || username.isBlank()) throw new IllegalArgumentException("username is required");
        PasswordValidator.validate(rawPassword);
        if (appUserRepository.findByUsername(username).isPresent()) throw new IllegalArgumentException("username already used");

        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        appUserRepository.save(user);
    }

    @Transactional
    public Map<String, Object> login(String username, String rawPassword) {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("invalid credentials"));

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("invalid credentials");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        UserSession session = new UserSession();
        session.setUser(user);
        session.setRefreshTokenId(jwtTokenProvider.getTokenId(refreshToken));
        session.setStatus(SessionStatus.ACTIVE);
        session.setCreatedAt(Instant.now());
        session.setUpdatedAt(Instant.now());
        session.setExpiresAt(jwtTokenProvider.getExpiration(refreshToken));
        userSessionRepository.save(session);

        return Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken,
                "tokenType", "Bearer"
        );
    }

    @Transactional
    public Map<String, Object> refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("refresh token is required");
        }
        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("invalid refresh token");
        }

        String refreshTokenId = jwtTokenProvider.getTokenId(refreshToken);
        UserSession activeSession = userSessionRepository
                .findByRefreshTokenIdAndStatus(refreshTokenId, SessionStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("refresh token is not active"));

        if (activeSession.getExpiresAt().isBefore(Instant.now())) {
            activeSession.setStatus(SessionStatus.EXPIRED);
            activeSession.setUpdatedAt(Instant.now());
            userSessionRepository.save(activeSession);
            throw new IllegalArgumentException("refresh token expired");
        }

        String tokenUsername = jwtTokenProvider.getUsername(refreshToken);
        if (!activeSession.getUser().getUsername().equals(tokenUsername)) {
            throw new IllegalArgumentException("invalid refresh token");
        }

        activeSession.setStatus(SessionStatus.REFRESHED);
        activeSession.setUpdatedAt(Instant.now());
        userSessionRepository.save(activeSession);

        AppUser user = activeSession.getUser();
        String newAccessToken = jwtTokenProvider.generateAccessToken(user);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user);

        UserSession newSession = new UserSession();
        newSession.setUser(user);
        newSession.setRefreshTokenId(jwtTokenProvider.getTokenId(newRefreshToken));
        newSession.setStatus(SessionStatus.ACTIVE);
        newSession.setCreatedAt(Instant.now());
        newSession.setUpdatedAt(Instant.now());
        newSession.setExpiresAt(jwtTokenProvider.getExpiration(newRefreshToken));
        userSessionRepository.save(newSession);

        return Map.of(
                "accessToken", newAccessToken,
                "refreshToken", newRefreshToken,
                "tokenType", "Bearer"
        );
    }

    @Transactional
    public Map<String, String> logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("refresh token is required");
        }
        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("invalid refresh token");
        }

        String refreshTokenId = jwtTokenProvider.getTokenId(refreshToken);
        UserSession session = userSessionRepository.findByRefreshTokenId(refreshTokenId)
                .orElseThrow(() -> new IllegalArgumentException("session not found"));

        if (session.getStatus() == SessionStatus.ACTIVE) {
            session.setStatus(SessionStatus.REVOKED);
            session.setUpdatedAt(Instant.now());
            userSessionRepository.save(session);
        }

        return Map.of("status", "logged_out");
    }
}

