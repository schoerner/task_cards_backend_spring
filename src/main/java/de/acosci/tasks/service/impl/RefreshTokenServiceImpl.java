package de.acosci.tasks.service.impl;

import de.acosci.tasks.model.entity.RefreshToken;
import de.acosci.tasks.model.entity.User;
import de.acosci.tasks.repository.RefreshTokenRepository;
import de.acosci.tasks.service.RefreshTokenService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${security.jwt.refresh-expiration-time:604800000}")
    private long refreshExpirationMs;

    @Override
    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(generateTokenValue());
        refreshToken.setExpiresAt(Instant.now().plusMillis(refreshExpirationMs));
        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public RefreshToken verifyUsableToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new EntityNotFoundException("Refresh-Token wurde nicht gefunden."));

        if (refreshToken.isRevoked()) {
            throw new IllegalStateException("Refresh-Token wurde bereits widerrufen.");
        }

        if (refreshToken.isExpired()) {
            throw new IllegalStateException("Refresh-Token ist abgelaufen.");
        }

        return refreshToken;
    }

    @Override
    public RefreshToken rotateRefreshToken(RefreshToken oldToken) {
        oldToken.setRevokedAt(Instant.now());
        refreshTokenRepository.save(oldToken);

        return createRefreshToken(oldToken.getUser());
    }

    @Override
    public void revokeToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(existing -> {
            existing.setRevokedAt(Instant.now());
            refreshTokenRepository.save(existing);
        });
    }

    @Override
    public void revokeAllForUser(Long userId) {
        var tokens = refreshTokenRepository.findAllByUser_IdAndRevokedAtIsNull(userId);
        tokens.forEach(token -> token.setRevokedAt(Instant.now()));
        refreshTokenRepository.saveAll(tokens);
    }

    private String generateTokenValue() {
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}