package com.aryanjais.aijobagent.scheduler;

import com.aryanjais.aijobagent.service.DailyDigestService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DigestScheduler {

    private static final Logger log = LoggerFactory.getLogger(DigestScheduler.class);

    private final DailyDigestService dailyDigestService;

    @Scheduled(cron = "${app.notification.daily-digest-cron}")
    public void runDailyDigest() {
        log.info("Daily digest scheduler triggered");
        dailyDigestService.sendDailyDigests();
    }
}
