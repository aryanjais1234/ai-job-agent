package com.aryanjais.aijobagent.service;

import com.aryanjais.aijobagent.entity.User;
import com.aryanjais.aijobagent.exception.ResourceNotFoundException;
import com.aryanjais.aijobagent.repository.ApplicationRepository;
import com.aryanjais.aijobagent.repository.CoverLetterRepository;
import com.aryanjais.aijobagent.repository.JobMatchRepository;
import com.aryanjais.aijobagent.repository.NotificationLogRepository;
import com.aryanjais.aijobagent.repository.NotificationPreferenceRepository;
import com.aryanjais.aijobagent.repository.ResumeRepository;
import com.aryanjais.aijobagent.repository.TailoredResumeRepository;
import com.aryanjais.aijobagent.repository.UserPreferenceRepository;
import com.aryanjais.aijobagent.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserPreferenceRepository userPreferenceRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private JobMatchRepository jobMatchRepository;

    @Mock
    private TailoredResumeRepository tailoredResumeRepository;

    @Mock
    private CoverLetterRepository coverLetterRepository;

    @Mock
    private NotificationLogRepository notificationLogRepository;

    @Mock
    private NotificationPreferenceRepository notificationPreferenceRepository;

    @Mock
    private ResumeRepository resumeRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .passwordHash("encoded_password")
                .fullName("Test User")
                .phone("+91-9876543210")
                .linkedinUrl("https://linkedin.com/in/test")
                .githubUrl("https://github.com/test")
                .portfolioUrl("https://test.dev")
                .location("Bangalore")
                .experienceYears(5)
                .isActive(true)
                .emailVerified(true)
                .verificationToken("some-token")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void deleteAccount_existingUser_softDeletesAndAnonymizes() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(testUser)).thenReturn(testUser);

        userService.deleteAccount(1L);

        // Verify all related records are deleted
        verify(notificationLogRepository).deleteByUserId(1L);
        verify(notificationPreferenceRepository).deleteByUserId(1L);
        verify(applicationRepository).deleteByUserId(1L);
        verify(coverLetterRepository).deleteByUserId(1L);
        verify(tailoredResumeRepository).deleteByUserId(1L);
        verify(jobMatchRepository).deleteByUserId(1L);
        verify(resumeRepository).deleteByUserId(1L);
        verify(userPreferenceRepository).deleteByUserId(1L);

        // Verify user is soft-deleted and anonymized
        assertFalse(testUser.getIsActive());
        assertEquals("deleted_1@deactivated.local", testUser.getEmail());
        assertEquals("Deleted User", testUser.getFullName());
        assertNull(testUser.getPhone());
        assertNull(testUser.getLinkedinUrl());
        assertNull(testUser.getGithubUrl());
        assertNull(testUser.getPortfolioUrl());
        assertNull(testUser.getLocation());
        assertNull(testUser.getVerificationToken());

        verify(userRepository).save(testUser);
    }

    @Test
    void deleteAccount_nonExistentUser_throwsResourceNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.deleteAccount(999L));
    }

    @Test
    void getProfile_existingUser_returnsUserResponse() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        var response = userService.getProfile(1L);

        assertEquals(1L, response.getId());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("Test User", response.getFullName());
        assertEquals("Bangalore", response.getLocation());
        assertEquals(5, response.getExperienceYears());
    }

    @Test
    void getProfile_nonExistentUser_throwsResourceNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getProfile(999L));
    }
}
