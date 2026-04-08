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
public class ApplicationResponse {
    private Long id;
    private Long jobId;
    private String jobTitle;
    private String company;
    private String location;
    private String status;
    private LocalDateTime appliedAt;
    private LocalDateTime interviewDate;
    private String notes;
    private boolean hasTailoredResume;
    private boolean hasCoverLetter;
    private LocalDateTime createdAt;
}
