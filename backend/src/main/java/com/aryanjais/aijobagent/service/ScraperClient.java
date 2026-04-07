package com.aryanjais.aijobagent.service;

import com.aryanjais.aijobagent.dto.request.TriggerScrapeRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * HTTP client for calling the Python scraper-service endpoints.
 * Triggers scraping on individual or all platforms.
 */
@Service
public class ScraperClient {

    private static final Logger log = LoggerFactory.getLogger(ScraperClient.class);

    private final RestClient restClient;

    public ScraperClient(
            @Value("${app.scraper.base-url}") String scraperBaseUrl,
            @Value("${app.scraper.timeout-seconds:300}") int timeoutSeconds,
            RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
                .baseUrl(scraperBaseUrl)
                .build();
        log.info("ScraperClient configured with base URL: {}", scraperBaseUrl);
    }

    /**
     * Trigger scraping on all platforms (Indeed, LinkedIn, Naukri).
     */
    public void triggerScrapeAll(TriggerScrapeRequest request) {
        triggerScrape("/scrape/all", request);
    }

    /**
     * Trigger scraping on a specific platform.
     */
    public void triggerScrapePlatform(String platform, TriggerScrapeRequest request) {
        String endpoint = "/scrape/" + platform.toLowerCase();
        triggerScrape(endpoint, request);
    }

    private void triggerScrape(String endpoint, TriggerScrapeRequest request) {
        try {
            restClient.post()
                    .uri(endpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Scrape triggered successfully: {}", endpoint);
        } catch (Exception e) {
            log.error("Failed to trigger scrape at {}: {}", endpoint, e.getMessage());
            throw new RuntimeException("Failed to trigger scrape: " + e.getMessage(), e);
        }
    }
}
