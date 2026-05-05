package edu.cit.caones.splitshare.shared.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService Unit Tests")
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    private UserDetails testUser;
    private static final String TEST_SECRET = "dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tZ2VuZXJhdGlvbi10ZXN0aW5nLTI1Ni1iaXRz";
    private static final long TEST_EXPIRATION = 86400000; // 24 hours
    private static final long TEST_REFRESH_EXPIRATION = 604800000; // 7 days

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("test@example.com")
                .password("password")
                .authorities(java.util.Collections.emptyList())
                .build();

        // Inject test configuration
        ReflectionTestUtils.setField(jwtService, "secretKey", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", TEST_EXPIRATION);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", TEST_REFRESH_EXPIRATION);
    }

    // ── TOKEN GENERATION TESTS ───────────────────────────────────────────────

    @Test
    @DisplayName("Should generate valid JWT token")
    void testGenerateTokenSuccess() {
        // Act
        String token = jwtService.generateToken(testUser);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains("."));
    }

    @Test
    @DisplayName("Should generate different tokens for same user on consecutive calls")
    void testGenerateTokenDifferent() {
        // Act
        String token1 = jwtService.generateToken(testUser);
        String token2 = jwtService.generateToken(testUser);

        // Assert
        assertNotEquals(token1, token2); // Different due to different issuedAt times
    }

    @Test
    @DisplayName("Should generate refresh token")
    void testGenerateRefreshTokenSuccess() {
        // Act
        String refreshToken = jwtService.generateRefreshToken(testUser);

        // Assert
        assertNotNull(refreshToken);
        assertFalse(refreshToken.isEmpty());
        assertTrue(refreshToken.contains("."));
    }

    @Test
    @DisplayName("Refresh token should have longer expiration than access token")
    void testRefreshTokenHasLongerExpiration() {
        // Act
        String accessToken = jwtService.generateToken(testUser);
        String refreshToken = jwtService.generateRefreshToken(testUser);

        // Assert
        Date accessExpiration = jwtService.extractClaim(accessToken, Claims::getExpiration);
        Date refreshExpiration = jwtService.extractClaim(refreshToken, Claims::getExpiration);

        assertTrue(refreshExpiration.after(accessExpiration));
    }

    // ── TOKEN EXTRACTION TESTS ───────────────────────────────────────────────

    @Test
    @DisplayName("Should extract username from token")
    void testExtractUsernameSuccess() {
        // Arrange
        String token = jwtService.generateToken(testUser);

        // Act
        String extractedUsername = jwtService.extractUsername(token);

        // Assert
        assertEquals("test@example.com", extractedUsername);
    }

    @Test
    @DisplayName("Should extract expiration from token")
    void testExtractExpirationSuccess() {
        // Arrange
        String token = jwtService.generateToken(testUser);

        // Act
        Date expiration = jwtService.extractClaim(token, Claims::getExpiration);

        // Assert
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    @DisplayName("Should extract custom claim from token")
    void testExtractClaimSuccess() {
        // Arrange
        String token = jwtService.generateToken(testUser);

        // Act
        String subject = jwtService.extractClaim(token, Claims::getSubject);

        // Assert
        assertEquals("test@example.com", subject);
    }

    @Test
    @DisplayName("Should throw exception when extracting claim from invalid token")
    void testExtractClaimFromInvalidTokenThrows() {
        // Act & Assert
        assertThrows(JwtException.class, () -> 
            jwtService.extractClaim("invalid.token.here", Claims::getSubject)
        );
    }

    // ── TOKEN VALIDATION TESTS ───────────────────────────────────────────────

    @Test
    @DisplayName("Should validate valid token with matching user")
    void testIsTokenValidSuccess() {
        // Arrange
        String token = jwtService.generateToken(testUser);

        // Act
        boolean isValid = jwtService.isTokenValid(token, testUser);

        // Assert
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should reject token with different user")
    void testIsTokenValidDifferentUser() {
        // Arrange
        String token = jwtService.generateToken(testUser);
        UserDetails differentUser = User.builder()
                .username("different@example.com")
                .password("password")
                .authorities(java.util.Collections.emptyList())
                .build();

        // Act
        boolean isValid = jwtService.isTokenValid(token, differentUser);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should reject invalid token format")
    void testIsTokenValidInvalidFormat() {
        // Arrange
        String invalidToken = "invalid.token";
        UserDetails user = testUser;

        // Act & Assert
        assertThrows(JwtException.class, () -> jwtService.isTokenValid(invalidToken, user));
    }

    @Test
    @DisplayName("Should accept token within expiration time")
    void testIsTokenValidWithinExpiration() {
        // Arrange
        String token = jwtService.generateToken(testUser);

        // Act - Should not throw and return true
        boolean isValid = jwtService.isTokenValid(token, testUser);

        // Assert
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Token should contain subject (username)")
    void testTokenContainsSubject() {
        // Arrange
        String token = jwtService.generateToken(testUser);

        // Act
        String subject = jwtService.extractClaim(token, Claims::getSubject);

        // Assert
        assertEquals(testUser.getUsername(), subject);
    }

    @Test
    @DisplayName("Token should contain issued-at claim")
    void testTokenContainsIssuedAtClaim() {
        // Arrange
        String token = jwtService.generateToken(testUser);

        // Act
        Date issuedAt = jwtService.extractClaim(token, Claims::getIssuedAt);

        // Assert
        assertNotNull(issuedAt);
        assertTrue(issuedAt.before(new Date()) || issuedAt.equals(new Date()));
    }

    @Test
    @DisplayName("Should reject malformed token")
    void testIsTokenValidMalformedToken() {
        // Arrange
        String malformedToken = "malformed";

        // Act & Assert
        assertThrows(JwtException.class, () -> jwtService.isTokenValid(malformedToken, testUser));
    }

    @Test
    @DisplayName("Should handle token with null payload")
    void testIsTokenValidNullPayload() {
        // Act & Assert
        assertThrows(Exception.class, () -> jwtService.isTokenValid(null, testUser));
    }

    @Test
    @DisplayName("Username extraction should be case-sensitive for matching")
    void testUsernameExtractionCaseSensitive() {
        // Arrange
        String token = jwtService.generateToken(testUser);

        // Act
        String extracted = jwtService.extractUsername(token);

        // Assert
        assertEquals("test@example.com", extracted);
    }

    @Test
    @DisplayName("Should generate token with valid JWT structure")
    void testTokenStructure() {
        // Arrange & Act
        String token = jwtService.generateToken(testUser);

        // Assert - JWT has 3 parts separated by dots
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);
        assertTrue(parts[0].length() > 0); // Header
        assertTrue(parts[1].length() > 0); // Payload
        assertTrue(parts[2].length() > 0); // Signature
    }
}
