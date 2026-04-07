package com.aryanjais.aijobagent.service;

import com.aryanjais.aijobagent.config.AiConfig;
import com.aryanjais.aijobagent.entity.*;
import com.aryanjais.aijobagent.entity.enums.AiOperationType;
import com.aryanjais.aijobagent.exception.AiServiceException;
import com.aryanjais.aijobagent.exception.ResourceNotFoundException;
import com.aryanjais.aijobagent.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CoverLetterService {

    private static final Logger log = LoggerFactory.getLogger(CoverLetterService.class);

    private final OpenAiClient openAiClient;
    private final AiConfig aiConfig;
    private final AiUsageLogService aiUsageLogService;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final JobAnalysisRepository jobAnalysisRepository;
    private final CoverLetterRepository coverLetterRepository;

    private static final String SYSTEM_PROMPT = """
            You are an expert career consultant who writes compelling, professional cover letters. \
            Write a personalized cover letter (250-350 words) that is specific to the job and company. \
            Return ONLY the cover letter text, no JSON, no markdown formatting.""";

    private static final String USER_PROMPT_TEMPLATE = """
            Write a cover letter for this candidate and job:
            
            Candidate:
            - Name: %s
            - Experience: %d years
            - Location: %s
            - Current/Recent Role: %s
            
            Target Job:
            - Title: %s
            - Company: %s
            - Location: %s
            - Domain: %s
            - Required Skills: %s
            - Description: %s
            
            Structure:
            1. Opening paragraph: Express specific interest in the role and company
            2. Body paragraph 1: Highlight relevant experiences with metrics/achievements
            3. Body paragraph 2: Demonstrate knowledge of the company and cultural fit
            4. Closing: Strong call to action
            
            Tone: Professional, confident, and genuine. 250-350 words.""";

    @Transactional
    public CoverLetter generateCoverLetter(Long userId, Long jobId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Optional<CoverLetter> existing = coverLetterRepository.findByUserIdAndJobId(userId, jobId);
        if (existing.isPresent()) {
            log.debug("Cover letter already exists for user {} job {}", userId, jobId);
            return existing.get();
        }

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + jobId));

        JobAnalysis analysis = jobAnalysisRepository.findByJobId(jobId).orElse(null);

        String userPrompt = String.format(USER_PROMPT_TEMPLATE,
                user.getFullName(),
                user.getExperienceYears() != null ? user.getExperienceYears() : 0,
                user.getLocation() != null ? user.getLocation() : "Not specified",
                "Software Professional",
                job.getTitle(),
                job.getCompany(),
                job.getLocation() != null ? job.getLocation() : "Not specified",
                analysis != null && analysis.getDomain() != null ? analysis.getDomain() : "Technology",
                analysis != null && analysis.getRequiredSkills() != null ? analysis.getRequiredSkills() : "[]",
                job.getDescription() != null ? job.getDescription().substring(0, Math.min(1000, job.getDescription().length())) : "");

        String model = aiConfig.getTailorModel();
        long startTime = System.currentTimeMillis();

        try {
            JsonNode apiResponse = openAiClient.chatCompletion(SYSTEM_PROMPT, userPrompt, model);
            long durationMs = System.currentTimeMillis() - startTime;

            String content = openAiClient.extractContent(apiResponse);
            OpenAiClient.UsageInfo usage = openAiClient.extractUsage(apiResponse);

            aiUsageLogService.logUsage(user, AiOperationType.COVER_LETTER, model,
                    usage.promptTokens(), usage.completionTokens(), usage.totalTokens(),
                    durationMs, true);

            CoverLetter coverLetter = CoverLetter.builder()
                    .user(user)
                    .job(job)
                    .content(content)
                    .build();

            CoverLetter saved = coverLetterRepository.save(coverLetter);
            log.info("Cover letter generated: user={}, job={}", userId, jobId);
            return saved;

        } catch (AiServiceException e) {
            long durationMs = System.currentTimeMillis() - startTime;
            aiUsageLogService.logFailure(user, AiOperationType.COVER_LETTER, model, durationMs);
            throw e;
        }
    }
}
