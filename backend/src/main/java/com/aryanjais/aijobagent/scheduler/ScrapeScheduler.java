package com.aryanjais.aijobagent.scheduler;

import com.aryanjais.aijobagent.dto.request.TriggerScrapeRequest;
import com.aryanjais.aijobagent.entity.enums.ScrapePlatform;
import com.aryanjais.aijobagent.service.ScraperClient;
import com.aryanjais.aijobagent.service.ScrapeLogService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Cron scheduler that triggers job scraping daily (T-2.7).
 * Default schedule: 6:00 AM IST (00:30 UTC) every day, configurable via application.yml.
 */
@Component
@RequiredArgsConstructor
public class ScrapeScheduler {

    private static final Logger log = LoggerFactory.getLogger(ScrapeScheduler.class);

    private final ScraperClient scraperClient;
    private final ScrapeLogService scrapeLogService;

    /**
     * Scheduled daily scrape trigger. Cron is configured via ${app.notification.scrape-cron}.
     * Default: "0 0 6 * * *" (6 AM server time; set to IST via server timezone or cron config).
     */
    @Scheduled(cron = "${app.notification.scrape-cron:0 0 6 * * *}")
    public void triggerDailyScrape() {
        log.info("Starting scheduled daily scrape...");

        var scrapeLog = scrapeLogService.startScrapeLog(ScrapePlatform.ALL);

        try {
            TriggerScrapeRequest request = TriggerScrapeRequest.builder()
                    .keywords("Software Engineer")
                    .location("India")
                    .maxResults(25)
                    .build();

            scraperClient.triggerScrapeAll(request);

            scrapeLogService.completeScrapeLog(scrapeLog.getId());
            log.info("Scheduled daily scrape triggered successfully");
        } catch (Exception e) {
            scrapeLogService.failScrapeLog(scrapeLog.getId(), e.getMessage());
            log.error("Scheduled daily scrape failed: {}", e.getMessage(), e);
        }
    }
}
