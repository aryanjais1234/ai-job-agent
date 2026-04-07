package com.aryanjais.aijobagent.controller;

import com.aryanjais.aijobagent.dto.request.UpdateNotificationPreferencesRequest;
import com.aryanjais.aijobagent.dto.response.NotificationPreferenceResponse;
import com.aryanjais.aijobagent.dto.response.NotificationResponse;
import com.aryanjais.aijobagent.entity.NotificationLog;
import com.aryanjais.aijobagent.entity.NotificationPreference;
import com.aryanjais.aijobagent.entity.User;
import com.aryanjais.aijobagent.exception.ResourceNotFoundException;
import com.aryanjais.aijobagent.repository.NotificationLogRepository;
import com.aryanjais.aijobagent.repository.NotificationPreferenceRepository;
import com.aryanjais.aijobagent.repository.UserRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Manages notification preferences and notification log queries.
 * Actual email sending is handled by the notification-service.
 */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications")
public class NotificationController {

    private final NotificationLogRepository notificationLogRepository;
    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getNotifications(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        User user = getUser(authentication);
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationResponse> result = notificationLogRepository
                .findByUserIdOrderBySentAtDesc(user.getId(), pageable)
                .map(this::toResponse);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}/read")
    @Transactional
    public ResponseEntity<Void> markAsRead(
            Authentication authentication, @PathVariable Long id) {
        User user = getUser(authentication);
        NotificationLog notification = notificationLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + id));
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Notification not found: " + id);
        }
        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        notificationLogRepository.save(notification);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/preferences")
    public ResponseEntity<NotificationPreferenceResponse> getPreferences(
            Authentication authentication) {
        User user = getUser(authentication);
        NotificationPreference prefs = notificationPreferenceRepository.findByUserId(user.getId())
                .orElseGet(() -> createDefaultPreferences(user));
        return ResponseEntity.ok(toPreferenceResponse(prefs));
    }

    @PutMapping("/preferences")
    @Transactional
    public ResponseEntity<NotificationPreferenceResponse> updatePreferences(
            Authentication authentication,
            @RequestBody UpdateNotificationPreferencesRequest request) {
        User user = getUser(authentication);
        NotificationPreference prefs = notificationPreferenceRepository.findByUserId(user.getId())
                .orElseGet(() -> createDefaultPreferences(user));

        if (request.getEmailEnabled() != null) prefs.setEmailEnabled(request.getEmailEnabled());
        if (request.getDailyDigest() != null) prefs.setDailyDigest(request.getDailyDigest());
        if (request.getMatchThreshold() != null) prefs.setMatchThreshold(request.getMatchThreshold());
        if (request.getNewJobAlerts() != null) prefs.setNewJobAlerts(request.getNewJobAlerts());
        if (request.getApplicationUpdates() != null) prefs.setApplicationUpdates(request.getApplicationUpdates());
        if (request.getDigestTime() != null) prefs.setDigestTime(request.getDigestTime());

        prefs = notificationPreferenceRepository.save(prefs);
        return ResponseEntity.ok(toPreferenceResponse(prefs));
    }

    private User getUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private NotificationPreference createDefaultPreferences(User user) {
        NotificationPreference prefs = NotificationPreference.builder()
                .user(user)
                .emailEnabled(true)
                .pushEnabled(false)
                .dailyDigest(true)
                .weeklyReport(false)
                .matchThreshold(70)
                .newJobAlerts(true)
                .applicationUpdates(true)
                .digestTime(LocalTime.of(7, 0))
                .build();
        return notificationPreferenceRepository.save(prefs);
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

    private NotificationResponse toResponse(NotificationLog notifLog) {
        return NotificationResponse.builder()
                .id(notifLog.getId())
                .type(notifLog.getNotificationType().name())
                .subject(notifLog.getSubject())
                .content(notifLog.getContent())
                .sentAt(notifLog.getSentAt())
                .isRead(notifLog.getIsRead())
                .readAt(notifLog.getReadAt())
                .build();
    }
}
