package com.aryanjais.aijobagent.service;

import com.aryanjais.aijobagent.dto.response.ScrapeLogResponse;
import com.aryanjais.aijobagent.entity.ScrapeLog;
import com.aryanjais.aijobagent.entity.enums.ScrapePlatform;
import com.aryanjais.aijobagent.entity.enums.ScrapeStatus;
import com.aryanjais.aijobagent.repository.ScrapeLogRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
/**
 * Service for managing scrape log entries (T-2.8).
 * Records the start, progress, and completion of scraping runs.
 */
@Service
@RequiredArgsConstructor
public class ScrapeLogService {

    private static final Logger log = LoggerFactory.getLogger(ScrapeLogService.class);

    private final ScrapeLogRepository scrapeLogRepository;

    /**
     * Create a new scrape log entry with RUNNING status.
     */
    @Transactional
    public ScrapeLog startScrapeLog(ScrapePlatform platform) {
        ScrapeLog scrapeLog = ScrapeLog.builder()
                .platform(platform)
                .status(ScrapeStatus.RUNNING)
                .jobsFound(0)
                .jobsNew(0)
                .jobsUpdated(0)
                .startedAt(LocalDateTime.now())
                .build();

        scrapeLog = scrapeLogRepository.save(scrapeLog);
        log.info("Started scrape log [id={}] for platform: {}", scrapeLog.getId(), platform);
        return scrapeLog;
    }

    /**
     * Increment the job counters on a scrape log entry.
     */
    @Transactional
    public void incrementCounters(Long scrapeLogId, boolean isNew) {
        scrapeLogRepository.findById(scrapeLogId).ifPresent(scrapeLog -> {
            scrapeLog.setJobsFound(scrapeLog.getJobsFound() + 1);
            if (isNew) {
                scrapeLog.setJobsNew(scrapeLog.getJobsNew() + 1);
            } else {
                scrapeLog.setJobsUpdated(scrapeLog.getJobsUpdated() + 1);
            }
            scrapeLogRepository.save(scrapeLog);
        });
    }

    /**
     * Mark a scrape log as completed successfully.
     */
    @Transactional
    public void completeScrapeLog(Long scrapeLogId) {
        scrapeLogRepository.findById(scrapeLogId).ifPresent(scrapeLog -> {
            scrapeLog.setStatus(ScrapeStatus.COMPLETED);
            scrapeLog.setCompletedAt(LocalDateTime.now());
            scrapeLogRepository.save(scrapeLog);
            log.info("Scrape log [id={}] completed: found={}, new={}, updated={}",
                    scrapeLogId, scrapeLog.getJobsFound(), scrapeLog.getJobsNew(), scrapeLog.getJobsUpdated());
        });
    }

    /**
     * Mark a scrape log as failed with an error message.
     */
    @Transactional
    public void failScrapeLog(Long scrapeLogId, String errorMessage) {
        scrapeLogRepository.findById(scrapeLogId).ifPresent(scrapeLog -> {
            scrapeLog.setStatus(ScrapeStatus.FAILED);
            scrapeLog.setErrorMessage(errorMessage);
            scrapeLog.setCompletedAt(LocalDateTime.now());
            scrapeLogRepository.save(scrapeLog);
            log.error("Scrape log [id={}] failed: {}", scrapeLogId, errorMessage);
        });
    }

    /**
     * Get all scrape logs with pagination (for admin view).
     */
    public Page<ScrapeLogResponse> getScrapeLogsPage(Pageable pageable) {
        return scrapeLogRepository.findAllByOrderByStartedAtDesc(pageable)
                .map(this::toResponse);
    }

    /**
     * Get scrape logs with optional status and platform filters.
     */
    public Page<ScrapeLogResponse> getScrapeLogsPage(ScrapeStatus status, ScrapePlatform platform,
                                                      Pageable pageable) {
        Page<ScrapeLog> page;
        if (status != null && platform != null) {
            page = scrapeLogRepository.findByStatusAndPlatformOrderByStartedAtDesc(status, platform, pageable);
        } else if (status != null) {
            page = scrapeLogRepository.findByStatusOrderByStartedAtDesc(status, pageable);
        } else if (platform != null) {
            page = scrapeLogRepository.findByPlatformOrderByStartedAtDesc(platform, pageable);
        } else {
            page = scrapeLogRepository.findAllByOrderByStartedAtDesc(pageable);
        }
        return page.map(this::toResponse);
    }

    private ScrapeLogResponse toResponse(ScrapeLog entity) {
        return ScrapeLogResponse.builder()
                .id(entity.getId())
                .platform(entity.getPlatform().name())
                .status(entity.getStatus().name())
                .jobsFound(entity.getJobsFound())
                .jobsNew(entity.getJobsNew())
                .jobsUpdated(entity.getJobsUpdated())
                .errorMessage(entity.getErrorMessage())
                .startedAt(entity.getStartedAt())
                .completedAt(entity.getCompletedAt())
                .build();
    }
}
