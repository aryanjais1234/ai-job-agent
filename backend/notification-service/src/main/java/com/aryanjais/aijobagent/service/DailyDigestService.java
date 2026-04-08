package com.aryanjais.aijobagent.service;

import com.aryanjais.aijobagent.entity.JobMatch;
import com.aryanjais.aijobagent.entity.NotificationPreference;
import com.aryanjais.aijobagent.entity.User;
import com.aryanjais.aijobagent.entity.enums.NotificationType;
import com.aryanjais.aijobagent.repository.JobMatchRepository;
import com.aryanjais.aijobagent.repository.NotificationPreferenceRepository;
import com.aryanjais.aijobagent.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DailyDigestService {

    private static final Logger log = LoggerFactory.getLogger(DailyDigestService.class);

    private final UserRepository userRepository;
    private final JobMatchRepository jobMatchRepository;
    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final NotificationService notificationService;

    public void sendDailyDigests() {
        log.info("Starting daily digest email generation");
        List<User> users = userRepository.findAll();

        int sentCount = 0;
        for (User user : users) {
            if (!user.getIsActive()) continue;

            NotificationPreference prefs = notificationPreferenceRepository
                    .findByUserId(user.getId()).orElse(null);
            if (prefs != null && !prefs.getDailyDigest()) continue;

            try {
                var matchPage = jobMatchRepository.findByUserIdOrderByOverallScoreDesc(
                        user.getId(), PageRequest.of(0, 5));
                List<JobMatch> topMatches = matchPage.getContent();

                if (topMatches.isEmpty()) continue;

                StringBuilder content = new StringBuilder();
                content.append("<p>Here are your top job matches for today:</p>");
                content.append("<table style='width:100%; border-collapse:collapse;'>");
                content.append("<tr style='background:#f3f4f6;'>");
                content.append("<th style='padding:8px; text-align:left;'>Job</th>");
                content.append("<th style='padding:8px; text-align:left;'>Company</th>");
                content.append("<th style='padding:8px; text-align:center;'>Score</th>");
                content.append("</tr>");

                for (JobMatch match : topMatches) {
                    content.append(String.format("""
                            <tr style='border-bottom:1px solid #e5e7eb;'>
                                <td style='padding:8px;'>%s</td>
                                <td style='padding:8px;'>%s</td>
                                <td style='padding:8px; text-align:center; font-weight:bold; color:#4f46e5;'>%s%%</td>
                            </tr>""",
                            match.getJob().getTitle(),
                            match.getJob().getCompany(),
                            match.getOverallScore()));
                }
                content.append("</table>");
                content.append("<p>Log in to your dashboard to view details and download tailored resumes.</p>");

                notificationService.sendNotification(
                        user.getId(),
                        NotificationType.DAILY_DIGEST,
                        "Your Daily Job Matches - " + topMatches.size() + " new matches!",
                        content.toString());
                sentCount++;

            } catch (Exception e) {
                log.error("Failed to send digest for user {}: {}", user.getId(), e.getMessage());
            }
        }
        log.info("Daily digest completed: sent {} digests", sentCount);
    }
}
