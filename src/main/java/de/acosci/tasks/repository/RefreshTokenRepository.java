package de.acosci.tasks.repository;

import de.acosci.tasks.model.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    List<RefreshToken> findAllByUser_IdAndRevokedAtIsNull(Long userId);
    void deleteAllByUser_Id(Long userId);
}