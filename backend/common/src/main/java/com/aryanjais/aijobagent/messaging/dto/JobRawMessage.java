package com.aryanjais.aijobagent.messaging.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO representing a raw scraped job message consumed from the jobs.raw RabbitMQ queue.
 * Matches the JSON schema published by the Python scraper-service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class JobRawMessage {

    private String title;

    private String company;

    private String location;

    private String description;

    private String requirements;

    @JsonProperty("source_platform")
    private String sourcePlatform;

    @JsonProperty("source_url")
    private String sourceUrl;

    @JsonProperty("source_job_id")
    private String sourceJobId;

    @JsonProperty("job_type")
    private String jobType;

    @JsonProperty("salary_min")
    private BigDecimal salaryMin;

    @JsonProperty("salary_max")
    private BigDecimal salaryMax;

    @JsonProperty("posted_at")
    private String postedAt;

    @JsonProperty("scraped_at")
    private String scrapedAt;
}
