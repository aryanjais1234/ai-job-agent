package com.aryanjais.aijobagent.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeResponse {

    private Long id;

    private String originalFilename;

    private String filePath;

    private Boolean isPrimary;

    private Integer fileSizeBytes;

    private LocalDateTime createdAt;

    private List<SkillResponse> skills;

    private List<ExperienceResponse> experiences;

    private List<EducationResponse> educations;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SkillResponse {
        private Long id;
        private String skillName;
        private String skillCategory;
        private String proficiencyLevel;
        private java.math.BigDecimal yearsExperience;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExperienceResponse {
        private Long id;
        private String company;
        private String title;
        private java.time.LocalDate startDate;
        private java.time.LocalDate endDate;
        private String description;
        private Boolean isCurrent;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EducationResponse {
        private Long id;
        private String institution;
        private String degree;
        private String fieldOfStudy;
        private Integer graduationYear;
        private java.math.BigDecimal gpa;
    }
}
