package com.aryanjais.aijobagent.service;

import com.aryanjais.aijobagent.dto.response.AiUsageLogResponse;
import com.aryanjais.aijobagent.entity.AiUsageLog;
import com.aryanjais.aijobagent.entity.User;
import com.aryanjais.aijobagent.entity.enums.AiOperationType;
import com.aryanjais.aijobagent.repository.AiUsageLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiUsageLogServiceTest {

    @Mock
    private AiUsageLogRepository aiUsageLogRepository;

    @InjectMocks
    private AiUsageLogService aiUsageLogService;

    @Test
    void logUsage_persistsCorrectFields() {
        User user = User.builder().id(1L).build();
        when(aiUsageLogRepository.save(any(AiUsageLog.class))).thenAnswer(inv -> {
            AiUsageLog log = inv.getArgument(0);
            log.setId(1L);
            return log;
        });

        AiUsageLog result = aiUsageLogService.logUsage(
                user, AiOperationType.JD_ANALYSIS, "gpt-4o-mini",
                500, 200, 700, 1500L, true);

        assertNotNull(result);
        assertEquals(1L, result.getId());

        ArgumentCaptor<AiUsageLog> captor = ArgumentCaptor.forClass(AiUsageLog.class);
        verify(aiUsageLogRepository).save(captor.capture());
        AiUsageLog saved = captor.getValue();

        assertEquals(user, saved.getUser());
        assertEquals(AiOperationType.JD_ANALYSIS, saved.getOperationType());
        assertEquals("gpt-4o-mini", saved.getModelUsed());
        assertEquals(500, saved.getPromptTokens());
        assertEquals(200, saved.getCompletionTokens());
        assertEquals(700, saved.getTotalTokens());
        assertEquals(1500, saved.getDurationMs());
        assertTrue(saved.getSuccess());
        assertNotNull(saved.getCostUsd());
    }

    @Test
    void logFailure_persistsWithZeroTokens() {
        when(aiUsageLogRepository.save(any(AiUsageLog.class))).thenAnswer(inv -> {
            AiUsageLog log = inv.getArgument(0);
            log.setId(2L);
            return log;
        });

        aiUsageLogService.logFailure(null, AiOperationType.JD_ANALYSIS, "gpt-4o-mini", 500L);

        ArgumentCaptor<AiUsageLog> captor = ArgumentCaptor.forClass(AiUsageLog.class);
        verify(aiUsageLogRepository).save(captor.capture());
        AiUsageLog saved = captor.getValue();

        assertEquals(0, saved.getPromptTokens());
        assertEquals(0, saved.getCompletionTokens());
        assertEquals(0, saved.getTotalTokens());
        assertFalse(saved.getSuccess());
    }

    @Test
    void logUsage_costCalculation_isReasonable() {
        when(aiUsageLogRepository.save(any(AiUsageLog.class))).thenAnswer(inv -> inv.getArgument(0));

        AiUsageLog result = aiUsageLogService.logUsage(
                null, AiOperationType.SKILL_EXTRACT, "gpt-4o-mini",
                1000, 500, 1500, 2000L, true);

        assertNotNull(result.getCostUsd());
        // Cost should be > 0 for non-zero tokens
        assertTrue(result.getCostUsd().doubleValue() > 0);
    }

    @Test
    void getAiUsageLogs_returnsPaginatedResults() {
        AiUsageLog log1 = AiUsageLog.builder()
                .id(1L)
                .operationType(AiOperationType.JD_ANALYSIS)
                .modelUsed("gpt-4o-mini")
                .promptTokens(500)
                .completionTokens(200)
                .totalTokens(700)
                .costUsd(new BigDecimal("0.000195"))
                .durationMs(1500)
                .success(true)
                .createdAt(LocalDateTime.now())
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<AiUsageLog> page = new PageImpl<>(List.of(log1));
        when(aiUsageLogRepository.findAllByOrderByCreatedAtDesc(pageable)).thenReturn(page);

        Page<AiUsageLogResponse> result = aiUsageLogService.getAiUsageLogs(pageable);

        assertEquals(1, result.getTotalElements());
        AiUsageLogResponse response = result.getContent().get(0);
        assertEquals(1L, response.getId());
        assertEquals("JD_ANALYSIS", response.getOperationType());
        assertEquals("gpt-4o-mini", response.getModelUsed());
        assertEquals(700, response.getTotalTokens());
        assertTrue(response.getSuccess());
    }
}
