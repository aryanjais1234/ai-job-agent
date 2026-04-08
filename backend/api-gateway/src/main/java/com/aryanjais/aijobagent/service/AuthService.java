package com.aryanjais.aijobagent.service;

import com.aryanjais.aijobagent.config.JwtConfig;
import com.aryanjais.aijobagent.dto.request.ForgotPasswordRequest;
import com.aryanjais.aijobagent.dto.request.LoginRequest;
import com.aryanjais.aijobagent.dto.request.RefreshTokenRequest;
import com.aryanjais.aijobagent.dto.request.RegisterRequest;
import com.aryanjais.aijobagent.dto.request.ResetPasswordRequest;
import com.aryanjais.aijobagent.dto.response.AuthResponse;
import com.aryanjais.aijobagent.dto.response.MessageResponse;
import com.aryanjais.aijobagent.dto.response.UserResponse;
import com.aryanjais.aijobagent.entity.User;
import com.aryanjais.aijobagent.exception.DuplicateResourceException;
import com.aryanjais.aijobagent.exception.InvalidTokenException;
import com.aryanjais.aijobagent.exception.ResourceNotFoundException;
import com.aryanjais.aijobagent.repository.UserRepository;
import com.aryanjais.aijobagent.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtConfig jwtConfig;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .experienceYears(0)
                .isActive(true)
                .emailVerified(false)
                .verificationToken(UUID.randomUUID().toString())
                .build();

        user = userRepository.save(user);
        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        if (!user.getIsActive()) {
            throw new BadCredentialsException("Account is deactivated");
        }

        return buildAuthResponse(user);
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String token = request.getRefreshToken();
        String email = jwtService.extractEmail(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (jwtService.isTokenExpired(token)) {
            throw new InvalidTokenException("Refresh token has expired");
        }

        String newAccessToken = jwtService.generateAccessToken(user);
        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtConfig.getAccessTokenExpiryMs())
                .user(buildUserResponse(user))
                .build();
    }

    @Transactional
    public MessageResponse verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid verification token"));

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);

        return MessageResponse.builder()
                .message("Email verified successfully")
                .build();
    }

    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            user.setVerificationToken(UUID.randomUUID().toString());
            userRepository.save(user);
        });

        return MessageResponse.builder()
                .message("If an account exists with this email, a password reset link has been sent")
                .build();
    }

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByVerificationToken(request.getToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired reset token"));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setVerificationToken(null);
        userRepository.save(user);

        return MessageResponse.builder()
                .message("Password reset successfully")
                .build();
    }

    private AuthResponse buildAuthResponse(User user) {
        return AuthResponse.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .tokenType("Bearer")
                .expiresIn(jwtConfig.getAccessTokenExpiryMs())
                .user(buildUserResponse(user))
                .build();
    }

    private UserResponse buildUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .linkedinUrl(user.getLinkedinUrl())
                .githubUrl(user.getGithubUrl())
                .portfolioUrl(user.getPortfolioUrl())
                .location(user.getLocation())
                .experienceYears(user.getExperienceYears())
                .isActive(user.getIsActive())
                .emailVerified(user.getEmailVerified())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
