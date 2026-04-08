package com.aryanjais.aijobagent.service;

import com.aryanjais.aijobagent.dto.response.AiUsageLogResponse;
import com.aryanjais.aijobagent.entity.AiUsageLog;
import com.aryanjais.aijobagent.entity.User;
import com.aryanjais.aijobagent.entity.enums.AiOperationType;
import com.aryanjais.aijobagent.repository.AiUsageLogRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Service to log AI API token usage and cost (T-3.8).
 */
@Service
@RequiredArgsConstructor
public class AiUsageLogService {

    private static final Logger log = LoggerFactory.getLogger(AiUsageLogService.class);

    private final AiUsageLogRepository aiUsageLogRepository;

    // Pricing per 1M tokens (gpt-4o-mini) — approximate
    private static final BigDecimal INPUT_PRICE_PER_TOKEN = new BigDecimal("0.00000015");
    private static final BigDecimal OUTPUT_PRICE_PER_TOKEN = new BigDecimal("0.0000006");

    /**
     * Log an AI operation's usage metrics.
     */
    public AiUsageLog logUsage(User user, AiOperationType operationType, String model,
                               int promptTokens, int completionTokens, int totalTokens,
                               long durationMs, boolean success) {

        BigDecimal cost = estimateCost(promptTokens, completionTokens);

        AiUsageLog usageLog = AiUsageLog.builder()
                .user(user)
                .operationType(operationType)
                .modelUsed(model)
                .promptTokens(promptTokens)
                .completionTokens(completionTokens)
                .totalTokens(totalTokens)
                .costUsd(cost)
                .durationMs((int) durationMs)
                .success(success)
                .build();

        AiUsageLog saved = aiUsageLogRepository.save(usageLog);
        log.info("AI usage logged: op={}, model={}, tokens={}, cost=${}, duration={}ms, success={}",
                operationType, model, totalTokens, cost, durationMs, success);
        return saved;
    }

    /**
     * Log a failed AI operation.
     */
    public void logFailure(User user, AiOperationType operationType, String model, long durationMs) {
        logUsage(user, operationType, model, 0, 0, 0, durationMs, false);
    }

    /**
     * Get paginated AI usage logs (for admin view).
     */
    public Page<AiUsageLogResponse> getAiUsageLogs(Pageable pageable) {
        return aiUsageLogRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toResponse);
    }

    private BigDecimal estimateCost(int promptTokens, int completionTokens) {
        BigDecimal inputCost = INPUT_PRICE_PER_TOKEN.multiply(BigDecimal.valueOf(promptTokens));
        BigDecimal outputCost = OUTPUT_PRICE_PER_TOKEN.multiply(BigDecimal.valueOf(completionTokens));
        return inputCost.add(outputCost).setScale(6, RoundingMode.HALF_UP);
    }

    private AiUsageLogResponse toResponse(AiUsageLog entity) {
        return AiUsageLogResponse.builder()
                .id(entity.getId())
                .operationType(entity.getOperationType() != null ? entity.getOperationType().name() : null)
                .modelUsed(entity.getModelUsed())
                .promptTokens(entity.getPromptTokens())
                .completionTokens(entity.getCompletionTokens())
                .totalTokens(entity.getTotalTokens())
                .costUsd(entity.getCostUsd())
                .durationMs(entity.getDurationMs())
                .success(entity.getSuccess())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
