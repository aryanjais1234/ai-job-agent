package com.aryanjais.aijobagent.service;

import com.aryanjais.aijobagent.dto.request.UpdatePreferencesRequest;
import com.aryanjais.aijobagent.dto.request.UpdateProfileRequest;
import com.aryanjais.aijobagent.dto.response.UserResponse;
import com.aryanjais.aijobagent.entity.User;
import com.aryanjais.aijobagent.entity.UserPreference;
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final ApplicationRepository applicationRepository;
    private final JobMatchRepository jobMatchRepository;
    private final TailoredResumeRepository tailoredResumeRepository;
    private final CoverLetterRepository coverLetterRepository;
    private final NotificationLogRepository notificationLogRepository;
    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final ResumeRepository resumeRepository;
    private final ObjectMapper objectMapper;

    public UserResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return buildUserResponse(user);
    }

    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getLinkedinUrl() != null) {
            user.setLinkedinUrl(request.getLinkedinUrl());
        }
        if (request.getGithubUrl() != null) {
            user.setGithubUrl(request.getGithubUrl());
        }
        if (request.getPortfolioUrl() != null) {
            user.setPortfolioUrl(request.getPortfolioUrl());
        }
        if (request.getLocation() != null) {
            user.setLocation(request.getLocation());
        }
        if (request.getExperienceYears() != null) {
            user.setExperienceYears(request.getExperienceYears());
        }

        user = userRepository.save(user);
        return buildUserResponse(user);
    }

    public UserPreference getPreferences(Long userId) {
        return userPreferenceRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Preferences not found for user: " + userId));
    }

    @Transactional
    public UserPreference updatePreferences(Long userId, UpdatePreferencesRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        UserPreference preference = userPreferenceRepository.findByUserId(userId)
                .orElse(UserPreference.builder()
                        .user(user)
                        .remoteOk(false)
                        .build());

        try {
            if (request.getJobTitles() != null) {
                preference.setJobTitles(objectMapper.writeValueAsString(request.getJobTitles()));
            }
            if (request.getLocations() != null) {
                preference.setLocations(objectMapper.writeValueAsString(request.getLocations()));
            }
            if (request.getMinSalary() != null) {
                preference.setMinSalary(request.getMinSalary());
            }
            if (request.getMaxSalary() != null) {
                preference.setMaxSalary(request.getMaxSalary());
            }
            if (request.getJobTypes() != null) {
                preference.setJobTypes(objectMapper.writeValueAsString(request.getJobTypes()));
            }
            if (request.getRemoteOk() != null) {
                preference.setRemoteOk(request.getRemoteOk());
            }
            if (request.getExperienceLevels() != null) {
                preference.setExperienceLevels(objectMapper.writeValueAsString(request.getExperienceLevels()));
            }
            if (request.getSkillsRequired() != null) {
                preference.setSkillsRequired(objectMapper.writeValueAsString(request.getSkillsRequired()));
            }
            if (request.getIndustries() != null) {
                preference.setIndustries(objectMapper.writeValueAsString(request.getIndustries()));
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize preference data", e);
        }

        return userPreferenceRepository.save(preference);
    }

    /**
     * Soft-delete a user account by deactivating it and cleaning up associated data.
     */
    @Transactional
    public void deleteAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Remove related records in child-to-parent order to respect FK constraints
        notificationLogRepository.deleteByUserId(userId);
        notificationPreferenceRepository.deleteByUserId(userId);
        applicationRepository.deleteByUserId(userId);
        coverLetterRepository.deleteByUserId(userId);
        tailoredResumeRepository.deleteByUserId(userId);
        jobMatchRepository.deleteByUserId(userId);
        resumeRepository.deleteByUserId(userId);
        userPreferenceRepository.deleteByUserId(userId);

        // Soft-delete: deactivate user, anonymize PII
        user.setIsActive(false);
        user.setEmail("deleted_" + userId + "@deactivated.local");
        user.setFullName("Deleted User");
        user.setPhone(null);
        user.setLinkedinUrl(null);
        user.setGithubUrl(null);
        user.setPortfolioUrl(null);
        user.setLocation(null);
        user.setVerificationToken(null);
        userRepository.save(user);

        log.info("User account soft-deleted and anonymized: userId={}", userId);
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
