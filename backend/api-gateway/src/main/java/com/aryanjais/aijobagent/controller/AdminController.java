package com.aryanjais.aijobagent.controller;

import com.aryanjais.aijobagent.dto.request.TriggerScrapeRequest;
import com.aryanjais.aijobagent.dto.response.AiUsageLogResponse;
import com.aryanjais.aijobagent.dto.response.MessageResponse;
import com.aryanjais.aijobagent.dto.response.ScrapeLogResponse;
import com.aryanjais.aijobagent.entity.enums.ScrapePlatform;
import com.aryanjais.aijobagent.entity.enums.ScrapeStatus;
import com.aryanjais.aijobagent.service.AiUsageLogService;
import com.aryanjais.aijobagent.service.ScraperClient;
import com.aryanjais.aijobagent.service.ScrapeLogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin endpoints for scrape logs and manual scrape triggers (T-2.8).
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin")
public class AdminController {

    private final ScrapeLogService scrapeLogService;
    private final ScraperClient scraperClient;
    private final AiUsageLogService aiUsageLogService;

    /**
     * GET /api/v1/admin/scrape-logs — View paginated scrape history with optional filters.
     */
    @GetMapping("/scrape-logs")
    public ResponseEntity<Page<ScrapeLogResponse>> getScrapeLogs(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String platform,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);

        ScrapeStatus scrapeStatus = null;
        ScrapePlatform scrapePlatform = null;

        if (status != null && !status.isBlank()) {
            try {
                scrapeStatus = ScrapeStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        }
        if (platform != null && !platform.isBlank()) {
            try {
                scrapePlatform = ScrapePlatform.valueOf(platform.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        }

        if (scrapeStatus != null || scrapePlatform != null) {
            return ResponseEntity.ok(scrapeLogService.getScrapeLogsPage(scrapeStatus, scrapePlatform, pageable));
        }
        return ResponseEntity.ok(scrapeLogService.getScrapeLogsPage(pageable));
    }

    /**
     * POST /api/v1/admin/scrape — Trigger a manual scrape on all platforms.
     */
    @PostMapping("/scrape")
    public ResponseEntity<MessageResponse> triggerManualScrape(
            @RequestBody(required = false) TriggerScrapeRequest request) {

        if (request == null) {
            request = TriggerScrapeRequest.builder()
                    .keywords("Software Engineer")
                    .location("India")
                    .maxResults(25)
                    .build();
        }

        var scrapeLog = scrapeLogService.startScrapeLog(ScrapePlatform.ALL);

        try {
            scraperClient.triggerScrapeAll(request);
            scrapeLogService.completeScrapeLog(scrapeLog.getId());
        } catch (Exception e) {
            scrapeLogService.failScrapeLog(scrapeLog.getId(), e.getMessage());
        }

        return ResponseEntity.ok(MessageResponse.builder()
                .message("Scrape triggered for all platforms")
                .build());
    }

    /**
     * GET /api/v1/admin/ai-usage — View paginated AI usage statistics.
     */
    @GetMapping("/ai-usage")
    public ResponseEntity<Page<AiUsageLogResponse>> getAiUsage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(aiUsageLogService.getAiUsageLogs(pageable));
    }
}
