package com.aryanjais.aijobagent.service;

import com.aryanjais.aijobagent.config.AiConfig;
import com.aryanjais.aijobagent.entity.Job;
import com.aryanjais.aijobagent.entity.JobAnalysis;
import com.aryanjais.aijobagent.entity.enums.AiOperationType;
import com.aryanjais.aijobagent.entity.enums.RemoteType;
import com.aryanjais.aijobagent.entity.enums.SeniorityLevel;
import com.aryanjais.aijobagent.entity.enums.SourcePlatform;
import com.aryanjais.aijobagent.exception.AiServiceException;
import com.aryanjais.aijobagent.repository.JobAnalysisRepository;
import com.aryanjais.aijobagent.repository.JobRepository;
import com.aryanjais.aijobagent.repository.JobSkillRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobAnalysisServiceTest {

    @Mock private OpenAiClient openAiClient;
    @Mock private AiConfig aiConfig;
    @Mock private AiUsageLogService aiUsageLogService;
    @Mock private JobRepository jobRepository;
    @Mock private JobAnalysisRepository jobAnalysisRepository;
    @Mock private JobSkillRepository jobSkillRepository;
    @Spy private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private JobAnalysisService jobAnalysisService;

    private Job testJob;

    @BeforeEach
    void setUp() {
        testJob = Job.builder()
                .id(1L)
                .title("Senior Java Developer")
                .company("TechCo")
                .location("Bangalore")
                .description("Build scalable microservices using Java and Spring Boot")
                .requirements("5+ years Java, Spring Boot, Docker")
                .sourcePlatform(SourcePlatform.LINKEDIN)
                .sourceUrl("https://linkedin.com/jobs/123")
                .isActive(true)
                .build();
    }

    @Test
    void analyzeJob_alreadyAnalysed_returnsExisting() {
        JobAnalysis existing = JobAnalysis.builder().id(100L).job(testJob).build();
        when(jobRepository.findById(1L)).thenReturn(Optional.of(testJob));
        when(jobAnalysisRepository.findByJobId(1L)).thenReturn(Optional.of(existing));

        JobAnalysis result = jobAnalysisService.analyzeJob(1L);

        assertEquals(100L, result.getId());
        verify(openAiClient, never()).chatCompletion(anyString(), anyString(), anyString());
    }

    @Test
    void analyzeJob_jobNotFound_throwsException() {
        when(jobRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(AiServiceException.class, () -> jobAnalysisService.analyzeJob(999L));
    }

    @Test
    void analyzeJob_successfulAnalysis_persistsResults() throws Exception {
        when(jobRepository.findById(1L)).thenReturn(Optional.of(testJob));
        when(jobAnalysisRepository.findByJobId(1L)).thenReturn(Optional.empty());
        when(aiConfig.getAnalysisModel()).thenReturn("gpt-4o-mini");

        String analysisContent = "{\"required_skills\": [\"Java\", \"Spring Boot\"], \"nice_to_have_skills\": [\"Docker\"], \"experience_min\": 5, \"experience_max\": 10, \"education_required\": \"B.Tech CS\", \"domain\": \"FinTech\", \"keywords\": [\"microservices\", \"Java\"], \"seniority_level\": \"SENIOR\", \"remote_type\": \"HYBRID\"}";

        String aiResponseJson = """
                {
                    "choices": [{
                        "message": {
                            "content": "dummy"
                        }
                    }],
                    "usage": {"prompt_tokens": 500, "completion_tokens": 200, "total_tokens": 700}
                }
                """;
        // Pre-parse so the spy doesn't interfere with when() stubbing
        com.fasterxml.jackson.databind.JsonNode parsedResponse = new ObjectMapper().readTree(aiResponseJson);

        when(openAiClient.chatCompletion(anyString(), anyString(), eq("gpt-4o-mini")))
                .thenReturn(parsedResponse);
        when(openAiClient.extractContent(any())).thenReturn(analysisContent);
        when(openAiClient.extractUsage(any()))
                .thenReturn(new OpenAiClient.UsageInfo(500, 200, 700));
        when(jobAnalysisRepository.save(any(JobAnalysis.class))).thenAnswer(inv -> {
            JobAnalysis a = inv.getArgument(0);
            a.setId(100L);
            return a;
        });

        JobAnalysis result = jobAnalysisService.analyzeJob(1L);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(5, result.getExperienceMin());
        assertEquals(10, result.getExperienceMax());
        assertEquals("FinTech", result.getDomain());
        assertEquals(SeniorityLevel.SENIOR, result.getSeniorityLevel());
        assertEquals(RemoteType.HYBRID, result.getRemoteType());
        assertNotNull(result.getAnalyzedAt());

        verify(jobSkillRepository).saveAll(any());
        verify(aiUsageLogService).logUsage(
                eq(null), eq(AiOperationType.JD_ANALYSIS), eq("gpt-4o-mini"),
                eq(500), eq(200), eq(700), anyLong(), eq(true));
    }

    @Test
    void analyzeJob_aiFailure_logsFailureAndThrows() {
        when(jobRepository.findById(1L)).thenReturn(Optional.of(testJob));
        when(jobAnalysisRepository.findByJobId(1L)).thenReturn(Optional.empty());
        when(aiConfig.getAnalysisModel()).thenReturn("gpt-4o-mini");
        when(openAiClient.chatCompletion(anyString(), anyString(), anyString()))
                .thenThrow(new AiServiceException("Rate limit exceeded"));

        assertThrows(AiServiceException.class, () -> jobAnalysisService.analyzeJob(1L));
        verify(aiUsageLogService).logFailure(
                eq(null), eq(AiOperationType.JD_ANALYSIS), eq("gpt-4o-mini"), anyLong());
    }
}
