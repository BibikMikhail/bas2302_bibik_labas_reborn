package com.example.smarthome.repository;

import com.example.smarthome.security.SessionStatus;
import com.example.smarthome.security.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    Optional<UserSession> findByRefreshTokenIdAndStatus(String refreshTokenId, SessionStatus status);
    Optional<UserSession> findByRefreshTokenId(String refreshTokenId);
}

