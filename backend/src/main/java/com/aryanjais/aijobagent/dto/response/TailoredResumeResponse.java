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
public class TailoredResumeResponse {
    private Long id;
    private Long jobId;
    private String jobTitle;
    private String company;
    private String tailoredContent;
    private String modificationsLog;
    private BigDecimal atsScore;
    private boolean pdfAvailable;
    private LocalDateTime createdAt;
}
