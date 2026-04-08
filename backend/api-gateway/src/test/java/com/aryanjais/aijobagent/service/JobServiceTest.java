package com.aryanjais.aijobagent.service;

import com.aryanjais.aijobagent.dto.response.JobMatchResponse;
import com.aryanjais.aijobagent.dto.response.JobResponse;
import com.aryanjais.aijobagent.entity.Job;
import com.aryanjais.aijobagent.entity.JobMatch;
import com.aryanjais.aijobagent.entity.enums.MatchStatus;
import com.aryanjais.aijobagent.entity.enums.SourcePlatform;
import com.aryanjais.aijobagent.exception.ResourceNotFoundException;
import com.aryanjais.aijobagent.repository.JobAnalysisRepository;
import com.aryanjais.aijobagent.repository.JobMatchRepository;
import com.aryanjais.aijobagent.repository.JobRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private JobAnalysisRepository jobAnalysisRepository;

    @Mock
    private JobMatchRepository jobMatchRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private JobService jobService;

    private Job testJob;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testJob = Job.builder()
                .id(1L)
                .title("Senior Java Developer")
                .company("Infosys")
                .location("Bangalore")
                .sourcePlatform(SourcePlatform.LINKEDIN)
                .sourceUrl("https://linkedin.com/jobs/view/123")
                .isActive(true)
                .scrapedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
        pageable = PageRequest.of(0, 10);
    }

    @Test
    void getJob_existingJob_returnsJobResponse() {
        when(jobRepository.findById(1L)).thenReturn(Optional.of(testJob));
        when(jobAnalysisRepository.findByJobId(1L)).thenReturn(Optional.empty());

        JobResponse response = jobService.getJob(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Senior Java Developer", response.getTitle());
        assertEquals("Infosys", response.getCompany());
    }

    @Test
    void getJob_nonExistentJob_throwsResourceNotFoundException() {
        when(jobRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> jobService.getJob(999L));
    }

    @Test
    void listJobs_returnsActiveJobs() {
        Page<Job> jobPage = new PageImpl<>(List.of(testJob));
        when(jobRepository.findByIsActiveTrue(pageable)).thenReturn(jobPage);
        when(jobAnalysisRepository.findByJobId(1L)).thenReturn(Optional.empty());

        Page<JobResponse> result = jobService.listJobs(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Senior Java Developer", result.getContent().get(0).getTitle());
    }

    @Test
    void searchJobs_withKeyword_returnsMatchingJobs() {
        Page<Job> jobPage = new PageImpl<>(List.of(testJob));
        when(jobRepository.searchByKeyword("Java", pageable)).thenReturn(jobPage);
        when(jobAnalysisRepository.findByJobId(1L)).thenReturn(Optional.empty());

        Page<JobResponse> result = jobService.searchJobs("Java", null, pageable);

        assertEquals(1, result.getTotalElements());
        verify(jobRepository).searchByKeyword("Java", pageable);
    }

    @Test
    void searchJobs_withLocation_returnsMatchingJobs() {
        Page<Job> jobPage = new PageImpl<>(List.of(testJob));
        when(jobRepository.searchByLocation("Bangalore", pageable)).thenReturn(jobPage);
        when(jobAnalysisRepository.findByJobId(1L)).thenReturn(Optional.empty());

        Page<JobResponse> result = jobService.searchJobs(null, "Bangalore", pageable);

        assertEquals(1, result.getTotalElements());
        verify(jobRepository).searchByLocation("Bangalore", pageable);
    }

    @Test
    void searchJobs_withKeywordAndLocation_returnsMatchingJobs() {
        Page<Job> jobPage = new PageImpl<>(List.of(testJob));
        when(jobRepository.searchByKeywordAndLocation("Java", "Bangalore", pageable)).thenReturn(jobPage);
        when(jobAnalysisRepository.findByJobId(1L)).thenReturn(Optional.empty());

        Page<JobResponse> result = jobService.searchJobs("Java", "Bangalore", pageable);

        assertEquals(1, result.getTotalElements());
        verify(jobRepository).searchByKeywordAndLocation("Java", "Bangalore", pageable);
    }

    @Test
    void searchJobs_noFilters_returnsAllActiveJobs() {
        Page<Job> jobPage = new PageImpl<>(List.of(testJob));
        when(jobRepository.findByIsActiveTrue(pageable)).thenReturn(jobPage);
        when(jobAnalysisRepository.findByJobId(1L)).thenReturn(Optional.empty());

        Page<JobResponse> result = jobService.searchJobs(null, null, pageable);

        assertEquals(1, result.getTotalElements());
        verify(jobRepository).findByIsActiveTrue(pageable);
    }

    @Test
    void getMatchesForUser_withMinScore_returnsFilteredMatches() {
        JobMatch match = JobMatch.builder()
                .id(101L)
                .overallScore(new BigDecimal("94.50"))
                .skillScore(new BigDecimal("96.00"))
                .experienceScore(new BigDecimal("100.00"))
                .locationScore(new BigDecimal("100.00"))
                .domainScore(new BigDecimal("70.00"))
                .status(MatchStatus.NEW)
                .job(testJob)
                .createdAt(LocalDateTime.now())
                .build();

        Page<JobMatch> matchPage = new PageImpl<>(List.of(match));
        when(jobMatchRepository.findByUserIdAndOverallScoreGreaterThanEqualOrderByOverallScoreDesc(
                eq(1L), eq(BigDecimal.valueOf(70.0)), any(Pageable.class))).thenReturn(matchPage);
        when(jobAnalysisRepository.findByJobId(1L)).thenReturn(Optional.empty());

        Page<JobMatchResponse> result = jobService.getMatchesForUser(1L, 70.0, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(new BigDecimal("94.50"), result.getContent().get(0).getOverallScore());
    }

    @Test
    void getMatchesForUser_noMinScore_returnsAllMatches() {
        JobMatch match = JobMatch.builder()
                .id(101L)
                .overallScore(new BigDecimal("50.00"))
                .skillScore(new BigDecimal("60.00"))
                .experienceScore(new BigDecimal("40.00"))
                .locationScore(new BigDecimal("50.00"))
                .domainScore(new BigDecimal("30.00"))
                .status(MatchStatus.NEW)
                .job(testJob)
                .createdAt(LocalDateTime.now())
                .build();

        Page<JobMatch> matchPage = new PageImpl<>(List.of(match));
        when(jobMatchRepository.findByUserIdOrderByOverallScoreDesc(1L, pageable)).thenReturn(matchPage);
        when(jobAnalysisRepository.findByJobId(1L)).thenReturn(Optional.empty());

        Page<JobMatchResponse> result = jobService.getMatchesForUser(1L, null, pageable);

        assertEquals(1, result.getTotalElements());
    }
}
