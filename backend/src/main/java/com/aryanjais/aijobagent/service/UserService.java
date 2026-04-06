package com.aryanjais.aijobagent.service;

import com.aryanjais.aijobagent.dto.request.UpdatePreferencesRequest;
import com.aryanjais.aijobagent.dto.request.UpdateProfileRequest;
import com.aryanjais.aijobagent.dto.response.UserResponse;
import com.aryanjais.aijobagent.entity.User;
import com.aryanjais.aijobagent.entity.UserPreference;
import com.aryanjais.aijobagent.exception.ResourceNotFoundException;
import com.aryanjais.aijobagent.repository.UserPreferenceRepository;
import com.aryanjais.aijobagent.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;
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
