package com.aryanjais.aijobagent.service;

import com.aryanjais.aijobagent.entity.Job;
import com.aryanjais.aijobagent.entity.enums.JobType;
import com.aryanjais.aijobagent.entity.enums.SourcePlatform;
import com.aryanjais.aijobagent.messaging.dto.JobRawMessage;
import com.aryanjais.aijobagent.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

/**
 * Service responsible for persisting scraped jobs to the database with deduplication.
 * Deduplication uses (source_platform, source_url) unique constraint (T-2.4).
 */
@Service
@RequiredArgsConstructor
public class JobPersistenceService {

    private static final Logger log = LoggerFactory.getLogger(JobPersistenceService.class);

    private final JobRepository jobRepository;

    /**
     * Persist a raw scraped job message to the database.
     * Returns true if a new job was inserted, false if it was a duplicate.
     */
    @Transactional
    public boolean persistJob(JobRawMessage message) {
        SourcePlatform platform = parseSourcePlatform(message.getSourcePlatform());
        String sourceUrl = truncateUrl(message.getSourceUrl());

        Optional<Job> existing = jobRepository.findBySourcePlatformAndSourceUrl(platform, sourceUrl);

        if (existing.isPresent()) {
            log.debug("Duplicate job skipped: {} @ {} [{}]", message.getTitle(), message.getCompany(), sourceUrl);
            return false;
        }

        Job job = Job.builder()
                .title(sanitize(message.getTitle(), 255))
                .company(sanitize(message.getCompany(), 255))
                .location(sanitize(message.getLocation(), 255))
                .jobType(parseJobType(message.getJobType()))
                .salaryMin(message.getSalaryMin())
                .salaryMax(message.getSalaryMax())
                .description(message.getDescription())
                .requirements(message.getRequirements())
                .sourcePlatform(platform)
                .sourceUrl(sourceUrl)
                .sourceJobId(message.getSourceJobId())
                .isActive(true)
                .postedAt(parseDateTime(message.getPostedAt()))
                .scrapedAt(parseDateTime(message.getScrapedAt()) != null
                        ? parseDateTime(message.getScrapedAt())
                        : LocalDateTime.now())
                .build();

        jobRepository.save(job);
        log.info("Persisted new job: {} @ {} [{}]", job.getTitle(), job.getCompany(), platform);
        return true;
    }

    private SourcePlatform parseSourcePlatform(String platform) {
        if (platform == null || platform.isBlank()) {
            throw new IllegalArgumentException("source_platform is required");
        }
        try {
            return SourcePlatform.valueOf(platform.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid source_platform: " + platform);
        }
    }

    private JobType parseJobType(String jobType) {
        if (jobType == null || jobType.isBlank()) {
            return null;
        }
        try {
            return JobType.valueOf(jobType.toUpperCase().trim().replace("-", "_").replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            log.warn("Unknown job_type '{}', ignoring", jobType);
            return null;
        }
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME);
        } catch (DateTimeParseException e) {
            try {
                return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
            } catch (DateTimeParseException e2) {
                log.warn("Could not parse datetime '{}', using null", dateTimeStr);
                return null;
            }
        }
    }

    private String sanitize(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() > maxLength ? trimmed.substring(0, maxLength) : trimmed;
    }

    private String truncateUrl(String url) {
        if (url == null) {
            return "";
        }
        return url.length() > 1000 ? url.substring(0, 1000) : url;
    }
}
