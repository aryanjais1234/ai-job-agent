package com.aryanjais.aijobagent.service;

import com.aryanjais.aijobagent.config.AiConfig;
import com.aryanjais.aijobagent.exception.AiServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * OpenAI HTTP client wrapper with retry logic and circuit breaker (T-3.1).
 * <p>
 * Circuit breaker trips after 5 consecutive failures, re-opens after 60 seconds.
 * Retry uses exponential backoff: 1s, 2s, 4s.
 */
@Service
@RequiredArgsConstructor
public class OpenAiClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiClient.class);

    private final AiConfig aiConfig;
    private final ObjectMapper objectMapper;

    // Circuit breaker state
    private static final int CIRCUIT_FAILURE_THRESHOLD = 5;
    private static final long CIRCUIT_RESET_MS = 60_000;
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    private final AtomicLong circuitOpenedAt = new AtomicLong(0);

    /**
     * Send a chat completion request to OpenAI.
     *
     * @param systemPrompt The system message
     * @param userPrompt   The user message
     * @param model        The model to use (e.g., gpt-4o-mini)
     * @return The parsed JSON response node containing the API response
     */
    public JsonNode chatCompletion(String systemPrompt, String userPrompt, String model) {
        checkCircuitBreaker();

        int maxAttempts = aiConfig.getRetryMaxAttempts();

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                JsonNode response = doRequest(systemPrompt, userPrompt, model);
                consecutiveFailures.set(0);
                return response;
            } catch (AiServiceException e) {
                if (attempt == maxAttempts) {
                    recordFailure();
                    throw e;
                }
                long backoffMs = (long) Math.pow(2, attempt - 1) * 1000;
                log.warn("OpenAI call attempt {}/{} failed, retrying in {}ms: {}",
                        attempt, maxAttempts, backoffMs, e.getMessage());
                sleep(backoffMs);
            }
        }

        throw new AiServiceException("OpenAI call failed after " + maxAttempts + " attempts");
    }

    private JsonNode doRequest(String systemPrompt, String userPrompt, String model) {
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", model);
            requestBody.put("max_tokens", aiConfig.getMaxTokens());
            requestBody.put("temperature", 0.1);

            ArrayNode messages = requestBody.putArray("messages");

            ObjectNode systemMessage = messages.addObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemPrompt);

            ObjectNode userMessage = messages.addObject();
            userMessage.put("role", "user");
            userMessage.put("content", userPrompt);

            String body = objectMapper.writeValueAsString(requestBody);

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(aiConfig.getTimeoutSeconds()))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(aiConfig.getBaseUrl() + "/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + aiConfig.getApiKey())
                    .timeout(Duration.ofSeconds(aiConfig.getTimeoutSeconds()))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 429) {
                throw new AiServiceException("OpenAI rate limit exceeded (HTTP 429)");
            }

            if (response.statusCode() >= 500) {
                throw new AiServiceException("OpenAI server error (HTTP " + response.statusCode() + ")");
            }

            if (response.statusCode() != 200) {
                throw new AiServiceException("OpenAI returned HTTP " + response.statusCode()
                        + ": " + response.body());
            }

            return objectMapper.readTree(response.body());
        } catch (AiServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new AiServiceException("OpenAI call failed: " + e.getMessage(), e);
        }
    }

    private void checkCircuitBreaker() {
        if (consecutiveFailures.get() >= CIRCUIT_FAILURE_THRESHOLD) {
            long openedAt = circuitOpenedAt.get();
            if (System.currentTimeMillis() - openedAt < CIRCUIT_RESET_MS) {
                throw new AiServiceException("Circuit breaker is open — AI service temporarily unavailable");
            }
            log.info("Circuit breaker half-open, allowing retry");
            consecutiveFailures.set(0);
        }
    }

    private void recordFailure() {
        int failures = consecutiveFailures.incrementAndGet();
        if (failures >= CIRCUIT_FAILURE_THRESHOLD) {
            circuitOpenedAt.set(System.currentTimeMillis());
            log.error("Circuit breaker OPEN after {} consecutive failures", failures);
        }
    }

    /**
     * Extract the content text from an OpenAI chat completion response.
     */
    public String extractContent(JsonNode response) {
        try {
            return response.get("choices").get(0).get("message").get("content").asText();
        } catch (Exception e) {
            throw new AiServiceException("Failed to extract content from OpenAI response", e);
        }
    }

    /**
     * Extract usage information from an OpenAI response.
     */
    public UsageInfo extractUsage(JsonNode response) {
        try {
            JsonNode usage = response.get("usage");
            return new UsageInfo(
                    usage.get("prompt_tokens").asInt(),
                    usage.get("completion_tokens").asInt(),
                    usage.get("total_tokens").asInt()
            );
        } catch (Exception e) {
            log.warn("Could not extract usage info from response: {}", e.getMessage());
            return new UsageInfo(0, 0, 0);
        }
    }

    public record UsageInfo(int promptTokens, int completionTokens, int totalTokens) {
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AiServiceException("Interrupted during retry backoff", e);
        }
    }
}
