package com.aryanjais.aijobagent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for match scoring thresholds.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.matching")
public class MatchingConfig {

    private double minimumScoreThreshold = 70.0;
    private double autoTailorThreshold = 75.0;
}
