package com.aryanjais.aijobagent.service;

import com.aryanjais.aijobagent.dto.response.JobMatchResponse;
import com.aryanjais.aijobagent.dto.response.JobResponse;
import com.aryanjais.aijobagent.entity.Job;
import com.aryanjais.aijobagent.entity.JobAnalysis;
import com.aryanjais.aijobagent.entity.JobMatch;
import com.aryanjais.aijobagent.exception.ResourceNotFoundException;
import com.aryanjais.aijobagent.repository.JobAnalysisRepository;
import com.aryanjais.aijobagent.repository.JobMatchRepository;
import com.aryanjais.aijobagent.repository.JobRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service for job listing and match retrieval (Phase 3 API endpoints).
 */
@Service
@RequiredArgsConstructor
public class JobService {

    private static final Logger log = LoggerFactory.getLogger(JobService.class);

    private final JobRepository jobRepository;
    private final JobAnalysisRepository jobAnalysisRepository;
    private final JobMatchRepository jobMatchRepository;
    private final ObjectMapper objectMapper;

    /**
     * Get a single job by ID with its analysis.
     */
    public JobResponse getJob(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));
        Optional<JobAnalysis> analysis = jobAnalysisRepository.findByJobId(jobId);
        return buildJobResponse(job, analysis.orElse(null));
    }

    /**
     * List all active jobs with pagination.
     */
    public Page<JobResponse> listJobs(Pageable pageable) {
        return jobRepository.findAll(pageable).map(job -> {
            Optional<JobAnalysis> analysis = jobAnalysisRepository.findByJobId(job.getId());
            return buildJobResponse(job, analysis.orElse(null));
        });
    }

    /**
     * Get paginated match results for a user, sorted by score descending.
     */
    public Page<JobMatchResponse> getMatchesForUser(Long userId, Pageable pageable) {
        return jobMatchRepository.findByUserIdOrderByOverallScoreDesc(userId, pageable)
                .map(this::buildJobMatchResponse);
    }

    private JobResponse buildJobResponse(Job job, JobAnalysis analysis) {
        JobResponse.JobResponseBuilder builder = JobResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .company(job.getCompany())
                .location(job.getLocation())
                .jobType(job.getJobType() != null ? job.getJobType().name() : null)
                .experienceRequired(job.getExperienceRequired())
                .salaryMin(job.getSalaryMin())
                .salaryMax(job.getSalaryMax())
                .description(job.getDescription())
                .requirements(job.getRequirements())
                .sourcePlatform(job.getSourcePlatform() != null ? job.getSourcePlatform().name() : null)
                .sourceUrl(job.getSourceUrl())
                .isActive(job.getIsActive())
                .postedAt(job.getPostedAt())
                .scrapedAt(job.getScrapedAt())
                .createdAt(job.getCreatedAt());

        if (analysis != null) {
            builder.analysis(JobResponse.AnalysisResponse.builder()
                    .requiredSkills(parseJsonList(analysis.getRequiredSkills()))
                    .niceToHaveSkills(parseJsonList(analysis.getNiceToHaveSkills()))
                    .experienceMin(analysis.getExperienceMin())
                    .experienceMax(analysis.getExperienceMax())
                    .educationRequired(analysis.getEducationRequired())
                    .domain(analysis.getDomain())
                    .keywords(parseJsonList(analysis.getKeywords()))
                    .seniorityLevel(analysis.getSeniorityLevel() != null ? analysis.getSeniorityLevel().name() : null)
                    .remoteType(analysis.getRemoteType() != null ? analysis.getRemoteType().name() : null)
                    .analyzedAt(analysis.getAnalyzedAt())
                    .build());
        }

        return builder.build();
    }

    private JobMatchResponse buildJobMatchResponse(JobMatch match) {
        Job job = match.getJob();
        Optional<JobAnalysis> analysis = jobAnalysisRepository.findByJobId(job.getId());

        return JobMatchResponse.builder()
                .matchId(match.getId())
                .overallScore(match.getOverallScore())
                .skillScore(match.getSkillScore())
                .experienceScore(match.getExperienceScore())
                .locationScore(match.getLocationScore())
                .domainScore(match.getDomainScore())
                .status(match.getStatus() != null ? match.getStatus().name() : null)
                .job(buildJobResponse(job, analysis.orElse(null)))
                .createdAt(match.getCreatedAt())
                .build();
    }

    private List<String> parseJsonList(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            log.warn("Failed to parse JSON list: {}", e.getMessage());
            return List.of();
        }
    }
}
