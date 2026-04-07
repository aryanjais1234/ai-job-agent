package com.aryanjais.aijobagent.service;

import com.aryanjais.aijobagent.dto.response.NotificationResponse;
import com.aryanjais.aijobagent.entity.NotificationLog;
import com.aryanjais.aijobagent.entity.NotificationPreference;
import com.aryanjais.aijobagent.entity.User;
import com.aryanjais.aijobagent.entity.enums.NotificationType;
import com.aryanjais.aijobagent.exception.ResourceNotFoundException;
import com.aryanjais.aijobagent.repository.NotificationLogRepository;
import com.aryanjais.aijobagent.repository.NotificationPreferenceRepository;
import com.aryanjais.aijobagent.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationLogRepository notificationLogRepository;
    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public void sendNotification(Long userId, NotificationType type, String subject, String content) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("User not found for notification: {}", userId);
            return;
        }

        NotificationPreference prefs = notificationPreferenceRepository.findByUserId(userId).orElse(null);

        // Check if notifications are enabled
        if (prefs != null && !prefs.getEmailEnabled()) {
            log.debug("Email notifications disabled for user {}", userId);
            return;
        }

        // Check type-specific preferences
        if (prefs != null) {
            if (type == NotificationType.DAILY_DIGEST && !prefs.getDailyDigest()) return;
            if (type == NotificationType.MATCH_ALERT && !prefs.getNewJobAlerts()) return;
            if (type == NotificationType.APPLICATION_UPDATE && !prefs.getApplicationUpdates()) return;
        }

        // Send email
        String htmlContent = buildEmailHtml(subject, content, user.getFullName());
        emailService.sendEmail(user.getEmail(), subject, htmlContent);

        // Log the notification
        NotificationLog notifLog = NotificationLog.builder()
                .user(user)
                .notificationType(type)
                .subject(subject)
                .content(content)
                .sentAt(LocalDateTime.now())
                .isRead(false)
                .build();
        notificationLogRepository.save(notifLog);
        log.info("Notification sent: user={}, type={}", userId, type);
    }

    public Page<NotificationResponse> getNotifications(Long userId, Pageable pageable) {
        return notificationLogRepository.findByUserIdOrderBySentAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        NotificationLog notification = notificationLogRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));
        if (!notification.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Notification not found: " + notificationId);
        }
        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        notificationLogRepository.save(notification);
    }

    public NotificationPreference getPreferences(Long userId) {
        return notificationPreferenceRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreferences(userId));
    }

    @Transactional
    public NotificationPreference updatePreferences(Long userId,
            Boolean emailEnabled, Boolean dailyDigest, Integer matchThreshold,
            Boolean newJobAlerts, Boolean applicationUpdates, LocalTime digestTime) {

        NotificationPreference prefs = notificationPreferenceRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreferences(userId));

        if (emailEnabled != null) prefs.setEmailEnabled(emailEnabled);
        if (dailyDigest != null) prefs.setDailyDigest(dailyDigest);
        if (matchThreshold != null) prefs.setMatchThreshold(matchThreshold);
        if (newJobAlerts != null) prefs.setNewJobAlerts(newJobAlerts);
        if (applicationUpdates != null) prefs.setApplicationUpdates(applicationUpdates);
        if (digestTime != null) prefs.setDigestTime(digestTime);

        return notificationPreferenceRepository.save(prefs);
    }

    private NotificationPreference createDefaultPreferences(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
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

    private String buildEmailHtml(String subject, String content, String userName) {
        return String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <div style="background: #4f46e5; color: white; padding: 20px; border-radius: 8px 8px 0 0;">
                        <h1 style="margin: 0;">AI Job Agent</h1>
                    </div>
                    <div style="padding: 20px; border: 1px solid #e5e7eb; border-top: none; border-radius: 0 0 8px 8px;">
                        <p>Hi %s,</p>
                        <h2>%s</h2>
                        <div>%s</div>
                        <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 20px 0;">
                        <p style="color: #6b7280; font-size: 12px;">
                            You received this email because you have notifications enabled.
                            Manage your notification preferences in the app settings.
                        </p>
                    </div>
                </body>
                </html>""", userName, subject, content.replace("\n", "<br>"));
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
