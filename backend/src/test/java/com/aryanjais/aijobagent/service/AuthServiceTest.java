package com.aryanjais.aijobagent.service;

import com.aryanjais.aijobagent.config.JwtConfig;
import com.aryanjais.aijobagent.dto.request.LoginRequest;
import com.aryanjais.aijobagent.dto.request.RegisterRequest;
import com.aryanjais.aijobagent.dto.request.ResetPasswordRequest;
import com.aryanjais.aijobagent.dto.response.AuthResponse;
import com.aryanjais.aijobagent.dto.response.MessageResponse;
import com.aryanjais.aijobagent.entity.User;
import com.aryanjais.aijobagent.exception.DuplicateResourceException;
import com.aryanjais.aijobagent.exception.InvalidTokenException;
import com.aryanjais.aijobagent.exception.ResourceNotFoundException;
import com.aryanjais.aijobagent.repository.UserRepository;
import com.aryanjais.aijobagent.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private JwtConfig jwtConfig;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .passwordHash("encoded_password")
                .fullName("Test User")
                .experienceYears(0)
                .isActive(true)
                .emailVerified(false)
                .verificationToken("test-token")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void register_newUser_returnsAuthResponse() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@example.com");
        request.setPassword("password123");
        request.setFullName("New User");

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");
        when(jwtConfig.getAccessTokenExpiryMs()).thenReturn(86400000L);

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_duplicateEmail_throwsDuplicateResourceException() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFullName("Test User");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(request));
    }

    @Test
    void login_validCredentials_returnsAuthResponse() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encoded_password")).thenReturn(true);
        when(jwtService.generateAccessToken(testUser)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(testUser)).thenReturn("refresh-token");
        when(jwtConfig.getAccessTokenExpiryMs()).thenReturn(86400000L);

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
    }

    @Test
    void login_invalidPassword_throwsBadCredentialsException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrong");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrong", "encoded_password")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void login_userNotFound_throwsResourceNotFoundException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("nobody@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.login(request));
    }

    @Test
    void verifyEmail_validToken_verifiesUser() {
        when(userRepository.findByVerificationToken("test-token")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        MessageResponse response = authService.verifyEmail("test-token");

        assertNotNull(response);
        assertTrue(response.getMessage().contains("verified"));
        assertTrue(testUser.getEmailVerified());
        assertNull(testUser.getVerificationToken());
        verify(userRepository).save(testUser);
    }

    @Test
    void verifyEmail_invalidToken_throwsInvalidTokenException() {
        when(userRepository.findByVerificationToken("invalid")).thenReturn(Optional.empty());

        assertThrows(InvalidTokenException.class, () -> authService.verifyEmail("invalid"));
    }

    @Test
    void resetPassword_validToken_updatesPassword() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("test-token");
        request.setNewPassword("newpassword123");

        when(userRepository.findByVerificationToken("test-token")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newpassword123")).thenReturn("new_encoded");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        MessageResponse response = authService.resetPassword(request);

        assertNotNull(response);
        assertTrue(response.getMessage().contains("reset"));
        assertEquals("new_encoded", testUser.getPasswordHash());
        assertNull(testUser.getVerificationToken());
        verify(userRepository).save(testUser);
    }
}
