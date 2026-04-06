package com.aryanjais.aijobagent.security;

import com.aryanjais.aijobagent.config.JwtConfig;
import com.aryanjais.aijobagent.entity.User;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Base64;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private JwtService jwtService;
    private User testUser;

    @BeforeEach
    void setUp() {
        JwtConfig config = new JwtConfig();
        // Generate a valid 256-bit base64-encoded secret
        byte[] keyBytes = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256).getEncoded();
        config.setSecret(Base64.getEncoder().encodeToString(keyBytes));
        config.setAccessTokenExpiryMs(86400000L); // 24 hours
        config.setRefreshTokenExpiryMs(604800000L); // 7 days

        jwtService = new JwtService(config);
        jwtService.init();

        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .passwordHash("hashed")
                .fullName("Test User")
                .experienceYears(0)
                .isActive(true)
                .emailVerified(true)
                .build();
    }

    @Test
    void generateAccessToken_validUser_returnsToken() {
        String token = jwtService.generateAccessToken(testUser);
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void generateAccessToken_containsCorrectEmail() {
        String token = jwtService.generateAccessToken(testUser);
        String email = jwtService.extractEmail(token);
        assertEquals("test@example.com", email);
    }

    @Test
    void generateAccessToken_containsUserId() {
        String token = jwtService.generateAccessToken(testUser);
        Long userId = jwtService.extractUserId(token);
        assertEquals(1L, userId);
    }

    @Test
    void generateRefreshToken_validUser_returnsToken() {
        String token = jwtService.generateRefreshToken(testUser);
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void generateRefreshToken_containsJti() {
        String token = jwtService.generateRefreshToken(testUser);
        String jti = jwtService.extractJti(token);
        assertNotNull(jti);
        assertFalse(jti.isEmpty());
    }

    @Test
    void extractEmail_validToken_returnsEmail() {
        String token = jwtService.generateAccessToken(testUser);
        assertEquals("test@example.com", jwtService.extractEmail(token));
    }

    @Test
    void extractUserId_validToken_returnsUserId() {
        String token = jwtService.generateAccessToken(testUser);
        assertEquals(1L, jwtService.extractUserId(token));
    }

    @Test
    void isTokenValid_validToken_returnsTrue() {
        String token = jwtService.generateAccessToken(testUser);
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                "test@example.com", "hashed", Collections.emptyList());
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void isTokenValid_wrongUser_returnsFalse() {
        String token = jwtService.generateAccessToken(testUser);
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                "other@example.com", "hashed", Collections.emptyList());
        assertFalse(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void isTokenExpired_validToken_returnsFalse() {
        String token = jwtService.generateAccessToken(testUser);
        assertFalse(jwtService.isTokenExpired(token));
    }

    @Test
    void extractEmail_invalidToken_throwsException() {
        assertThrows(Exception.class, () -> jwtService.extractEmail("invalid.token.here"));
    }

    @Test
    void generateRefreshToken_differentFromAccessToken() {
        String accessToken = jwtService.generateAccessToken(testUser);
        String refreshToken = jwtService.generateRefreshToken(testUser);
        assertFalse(accessToken.equals(refreshToken));
    }
}
