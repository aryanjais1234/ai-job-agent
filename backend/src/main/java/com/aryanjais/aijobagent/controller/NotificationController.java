package com.aryanjais.aijobagent.controller;

import com.aryanjais.aijobagent.dto.request.UpdateNotificationPreferencesRequest;
import com.aryanjais.aijobagent.dto.response.NotificationPreferenceResponse;
import com.aryanjais.aijobagent.dto.response.NotificationResponse;
import com.aryanjais.aijobagent.entity.NotificationPreference;
import com.aryanjais.aijobagent.entity.User;
import com.aryanjais.aijobagent.exception.ResourceNotFoundException;
import com.aryanjais.aijobagent.repository.UserRepository;
import com.aryanjais.aijobagent.service.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getNotifications(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        User user = getUser(authentication);
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(notificationService.getNotifications(user.getId(), pageable));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            Authentication authentication, @PathVariable Long id) {
        User user = getUser(authentication);
        notificationService.markAsRead(user.getId(), id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/preferences")
    public ResponseEntity<NotificationPreferenceResponse> getPreferences(
            Authentication authentication) {
        User user = getUser(authentication);
        NotificationPreference prefs = notificationService.getPreferences(user.getId());
        return ResponseEntity.ok(toPreferenceResponse(prefs));
    }

    @PutMapping("/preferences")
    public ResponseEntity<NotificationPreferenceResponse> updatePreferences(
            Authentication authentication,
            @RequestBody UpdateNotificationPreferencesRequest request) {
        User user = getUser(authentication);
        NotificationPreference prefs = notificationService.updatePreferences(
                user.getId(),
                request.getEmailEnabled(),
                request.getDailyDigest(),
                request.getMatchThreshold(),
                request.getNewJobAlerts(),
                request.getApplicationUpdates(),
                request.getDigestTime());
        return ResponseEntity.ok(toPreferenceResponse(prefs));
    }

    private User getUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private NotificationPreferenceResponse toPreferenceResponse(NotificationPreference prefs) {
        return NotificationPreferenceResponse.builder()
                .emailEnabled(prefs.getEmailEnabled())
                .dailyDigest(prefs.getDailyDigest())
                .matchThreshold(prefs.getMatchThreshold())
                .newJobAlerts(prefs.getNewJobAlerts())
                .applicationUpdates(prefs.getApplicationUpdates())
                .digestTime(prefs.getDigestTime())
                .build();
    }
}
