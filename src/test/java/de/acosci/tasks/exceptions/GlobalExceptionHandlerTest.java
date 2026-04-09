package de.acosci.tasks.exceptions;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("BadCredentialsException wird auf 401 gemappt")
    void handleSecurityException_badCredentials_returns401() {
        ProblemDetail result = handler.handleSecurityException(
                new BadCredentialsException("Bad credentials")
        );

        assertNotNull(result);
        assertEquals(401, result.getStatus());
        assertEquals("The username or password is incorrect", result.getProperties().get("description"));
    }

    @Test
    @DisplayName("SignatureException wird auf 401 gemappt")
    void handleSecurityException_signatureException_returns401() {
        ProblemDetail result = handler.handleSecurityException(
                new SignatureException("Invalid signature")
        );

        assertNotNull(result);
        assertEquals(401, result.getStatus());
    }

    @Test
    @DisplayName("ExpiredJwtException wird auf 401 gemappt")
    void handleSecurityException_expiredJwtException_returns401() {
        ProblemDetail result = handler.handleSecurityException(
                new ExpiredJwtException(null, null, "JWT expired")
        );

        assertNotNull(result);
        assertEquals(401, result.getStatus());
    }

    @Test
    @DisplayName("Unbekannte Exception wird auf 500 gemappt")
    void handleSecurityException_unknownException_returns500() {
        ProblemDetail result = handler.handleSecurityException(
                new RuntimeException("Boom")
        );

        assertNotNull(result);
        assertEquals(500, result.getStatus());
        assertEquals("Unknown internal server error.", result.getProperties().get("description"));
    }
}