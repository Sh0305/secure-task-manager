package com.securetask.taskmanager.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.securetask.taskmanager.exception.InvalidRequestException;
import com.securetask.taskmanager.model.RefreshToken;
import com.securetask.taskmanager.model.User;
import com.securetask.taskmanager.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        // Revoke all existing tokens for this user first
        refreshTokenRepository.revokeAllUserTokens(user);

        // Create fresh token
        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshExpiration))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(token);
    }

    public RefreshToken verifyRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenAndRevokedFalse(token)
                .orElseThrow(() -> new InvalidRequestException(
                        "Invalid or expired refresh token — please login again"));

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            revokeToken(refreshToken);
            throw new InvalidRequestException(
                    "Refresh token expired — please login again");
        }

        return refreshToken;
    }

    @Transactional
    public RefreshToken rotateRefreshToken(RefreshToken oldToken) {
        // Revoke old token
        revokeToken(oldToken);

        // Create new token for same user
        return createRefreshToken(oldToken.getUser());
    }

    @Transactional
    public void revokeToken(RefreshToken token) {
        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }

    @Transactional
    public void deleteUserTokens(User user) {
        refreshTokenRepository.revokeAllUserTokens(user);
    }
}
