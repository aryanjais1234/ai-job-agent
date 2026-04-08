package com.aryanjais.aijobagent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for OpenAI integration (T-3.1).
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.ai")
public class AiConfig {

    private String apiKey;
    private String baseUrl = "https://api.openai.com/v1";
    private String analysisModel = "gpt-4o-mini";
    private String tailorModel = "gpt-4o";
    private int maxTokens = 4096;
    private int timeoutSeconds = 60;
    private int retryMaxAttempts = 3;
}
