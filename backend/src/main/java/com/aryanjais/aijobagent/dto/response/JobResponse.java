package com.aryanjais.aijobagent.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobResponse {

    private Long id;
    private String title;
    private String company;
    private String location;
    private String jobType;
    private String experienceRequired;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String description;
    private String requirements;
    private String sourcePlatform;
    private String sourceUrl;
    private Boolean isActive;
    private LocalDateTime postedAt;
    private LocalDateTime scrapedAt;
    private LocalDateTime createdAt;
    private AnalysisResponse analysis;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AnalysisResponse {
        private List<String> requiredSkills;
        private List<String> niceToHaveSkills;
        private Integer experienceMin;
        private Integer experienceMax;
        private String educationRequired;
        private String domain;
        private List<String> keywords;
        private String seniorityLevel;
        private String remoteType;
        private LocalDateTime analyzedAt;
    }
}
