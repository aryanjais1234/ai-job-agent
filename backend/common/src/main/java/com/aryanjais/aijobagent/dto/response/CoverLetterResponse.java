package com.aryanjais.aijobagent.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoverLetterResponse {
    private Long id;
    private Long jobId;
    private String jobTitle;
    private String company;
    private String content;
    private boolean pdfAvailable;
    private LocalDateTime createdAt;
}
