package de.acosci.tasks.service.impl;

import de.acosci.tasks.model.entity.RefreshToken;
import de.acosci.tasks.model.entity.User;
import de.acosci.tasks.repository.RefreshTokenRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RefreshTokenServiceImplTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(refreshTokenService, "refreshExpirationMs", 604800000L);
    }

    @Test
    @DisplayName("createRefreshToken erstellt und speichert ein neues Refresh-Token")
    void createRefreshToken_shouldCreateAndSaveToken() {
        User user = new User();
        user.setId(1L);
        user.setEmail("max@test.de");

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);

        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken result = refreshTokenService.createRefreshToken(user);

        verify(refreshTokenRepository).save(captor.capture());

        RefreshToken savedToken = captor.getValue();
        assertNotNull(savedToken);
        assertEquals(user, savedToken.getUser());
        assertNotNull(savedToken.getToken());
        assertFalse(savedToken.getToken().isBlank());
        assertNotNull(savedToken.getExpiresAt());
        assertTrue(savedToken.getExpiresAt().isAfter(Instant.now()));

        assertNotNull(result);
        assertEquals(user, result.getUser());
        assertNotNull(result.getToken());
        assertFalse(result.getToken().isBlank());
    }

    @Test
    @DisplayName("verifyUsableToken liefert Token zurück, wenn es gültig und nicht widerrufen ist")
    void verifyUsableToken_withValidToken_shouldReturnToken() {
        User user = new User();
        user.setId(1L);

        RefreshToken token = new RefreshToken();
        token.setId(10L);
        token.setUser(user);
        token.setToken("valid-token");
        token.setExpiresAt(Instant.now().plusSeconds(3600));
        token.setRevokedAt(null);

        when(refreshTokenRepository.findByToken("valid-token"))
                .thenReturn(Optional.of(token));

        RefreshToken result = refreshTokenService.verifyUsableToken("valid-token");

        assertNotNull(result);
        assertEquals("valid-token", result.getToken());
        assertEquals(user, result.getUser());
    }

    @Test
    @DisplayName("verifyUsableToken wirft Exception, wenn Token nicht gefunden wird")
    void verifyUsableToken_whenTokenNotFound_shouldThrowEntityNotFoundException() {
        when(refreshTokenRepository.findByToken("missing-token"))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> refreshTokenService.verifyUsableToken("missing-token")
        );

        assertEquals("Refresh-Token wurde nicht gefunden.", exception.getMessage());
    }

    @Test
    @DisplayName("verifyUsableToken wirft Exception, wenn Token widerrufen wurde")
    void verifyUsableToken_whenTokenRevoked_shouldThrowIllegalStateException() {
        RefreshToken token = new RefreshToken();
        token.setToken("revoked-token");
        token.setExpiresAt(Instant.now().plusSeconds(3600));
        token.setRevokedAt(Instant.now());

        when(refreshTokenRepository.findByToken("revoked-token"))
                .thenReturn(Optional.of(token));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> refreshTokenService.verifyUsableToken("revoked-token")
        );

        assertEquals("Refresh-Token wurde bereits widerrufen.", exception.getMessage());
    }

    @Test
    @DisplayName("verifyUsableToken wirft Exception, wenn Token abgelaufen ist")
    void verifyUsableToken_whenTokenExpired_shouldThrowIllegalStateException() {
        RefreshToken token = new RefreshToken();
        token.setToken("expired-token");
        token.setExpiresAt(Instant.now().minusSeconds(60));
        token.setRevokedAt(null);

        when(refreshTokenRepository.findByToken("expired-token"))
                .thenReturn(Optional.of(token));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> refreshTokenService.verifyUsableToken("expired-token")
        );

        assertEquals("Refresh-Token ist abgelaufen.", exception.getMessage());
    }

    @Test
    @DisplayName("rotateRefreshToken widerruft altes Token und erstellt ein neues")
    void rotateRefreshToken_shouldRevokeOldTokenAndCreateNewToken() {
        User user = new User();
        user.setId(1L);
        user.setEmail("max@test.de");

        RefreshToken oldToken = new RefreshToken();
        oldToken.setId(10L);
        oldToken.setUser(user);
        oldToken.setToken("old-token");
        oldToken.setExpiresAt(Instant.now().plusSeconds(3600));
        oldToken.setRevokedAt(null);

        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken result = refreshTokenService.rotateRefreshToken(oldToken);

        assertNotNull(oldToken.getRevokedAt());
        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));

        assertNotNull(result);
        assertEquals(user, result.getUser());
        assertNotNull(result.getToken());
        assertFalse(result.getToken().isBlank());
        assertNotEquals("old-token", result.getToken());
        assertNotNull(result.getExpiresAt());
        assertTrue(result.getExpiresAt().isAfter(Instant.now()));
    }

    @Test
    @DisplayName("revokeToken setzt revokedAt, wenn Token existiert")
    void revokeToken_whenTokenExists_shouldSetRevokedAt() {
        RefreshToken token = new RefreshToken();
        token.setId(10L);
        token.setToken("token-to-revoke");
        token.setExpiresAt(Instant.now().plusSeconds(3600));
        token.setRevokedAt(null);

        when(refreshTokenRepository.findByToken("token-to-revoke"))
                .thenReturn(Optional.of(token));
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        refreshTokenService.revokeToken("token-to-revoke");

        assertNotNull(token.getRevokedAt());
        verify(refreshTokenRepository).save(token);
    }

    @Test
    @DisplayName("revokeToken tut nichts, wenn Token nicht existiert")
    void revokeToken_whenTokenDoesNotExist_shouldDoNothing() {
        when(refreshTokenRepository.findByToken("missing-token"))
                .thenReturn(Optional.empty());

        refreshTokenService.revokeToken("missing-token");

        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("revokeAllForUser widerruft alle aktiven Refresh-Tokens eines Users")
    void revokeAllForUser_shouldRevokeAllActiveTokens() {
        RefreshToken token1 = new RefreshToken();
        token1.setId(1L);
        token1.setToken("token-1");
        token1.setExpiresAt(Instant.now().plusSeconds(3600));

        RefreshToken token2 = new RefreshToken();
        token2.setId(2L);
        token2.setToken("token-2");
        token2.setExpiresAt(Instant.now().plusSeconds(3600));

        when(refreshTokenRepository.findAllByUser_IdAndRevokedAtIsNull(5L))
                .thenReturn(List.of(token1, token2));
        when(refreshTokenRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        refreshTokenService.revokeAllForUser(5L);

        verify(refreshTokenRepository).saveAll(anyList());
        assertNotNull(token1.getRevokedAt());
        assertNotNull(token2.getRevokedAt());
    }
}