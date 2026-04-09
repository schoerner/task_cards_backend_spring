package de.acosci.tasks.service;

import de.acosci.tasks.model.entity.RefreshToken;
import de.acosci.tasks.model.entity.User;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(User user);
    RefreshToken verifyUsableToken(String token);
    RefreshToken rotateRefreshToken(RefreshToken oldToken);
    void revokeToken(String token);
    void revokeAllForUser(Long userId);
}