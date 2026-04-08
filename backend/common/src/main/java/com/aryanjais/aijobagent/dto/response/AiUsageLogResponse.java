package com.aryanjais.aijobagent.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiUsageLogResponse {

    private Long id;

    private String operationType;

    private String modelUsed;

    private Integer promptTokens;

    private Integer completionTokens;

    private Integer totalTokens;

    private BigDecimal costUsd;

    private Integer durationMs;

    private Boolean success;

    private LocalDateTime createdAt;
}
