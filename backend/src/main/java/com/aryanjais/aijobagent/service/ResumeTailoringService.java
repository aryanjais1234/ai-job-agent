package com.aryanjais.aijobagent.service;

import com.aryanjais.aijobagent.config.AiConfig;
import com.aryanjais.aijobagent.entity.*;
import com.aryanjais.aijobagent.entity.enums.AiOperationType;
import com.aryanjais.aijobagent.exception.AiServiceException;
import com.aryanjais.aijobagent.exception.ResourceNotFoundException;
import com.aryanjais.aijobagent.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ResumeTailoringService {

    private static final Logger log = LoggerFactory.getLogger(ResumeTailoringService.class);

    private final OpenAiClient openAiClient;
    private final AiConfig aiConfig;
    private final AiUsageLogService aiUsageLogService;
    private final ResumeRepository resumeRepository;
    private final JobRepository jobRepository;
    private final JobAnalysisRepository jobAnalysisRepository;
    private final TailoredResumeRepository tailoredResumeRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    private static final String SYSTEM_PROMPT = """
            You are an expert ATS resume optimizer. Tailor the candidate's resume for the target job description. \
            Respond ONLY with valid JSON matching the schema below. Do NOT fabricate experience or skills the candidate does not have.""";

    private static final String USER_PROMPT_TEMPLATE = """
            Tailor this resume for the target job:
            
            Original Resume:
            ---
            %s
            ---
            
            Target Job:
            Title: %s
            Company: %s
            Description: %s
            Required Skills: %s
            Nice-to-have Skills: %s
            ATS Keywords: %s
            
            Instructions:
            - Reorder skills by relevance to the JD
            - Incorporate ATS keywords naturally into bullet points
            - Quantify achievements where possible
            - Strengthen action verbs
            - Add missing skills ONLY if the candidate genuinely has them based on their experience
            - Do NOT fabricate experience or responsibilities
            
            Return JSON:
            {
              "tailored_resume_text": "full tailored resume as formatted text",
              "modifications": [
                {"field": "string", "original": "string", "modified": "string", "reason": "string"}
              ],
              "ats_keywords_added": ["string"],
              "estimated_ats_score": number
            }""";

    @Transactional
    public TailoredResume tailorResume(Long userId, Long jobId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        // Check for existing tailored resume
        Optional<TailoredResume> existing = tailoredResumeRepository.findByUserIdAndJobId(userId, jobId);
        if (existing.isPresent()) {
            log.debug("Tailored resume already exists for user {} job {}", userId, jobId);
            return existing.get();
        }

        // Get primary resume
        Resume resume = resumeRepository.findByUserIdAndIsPrimary(userId, true)
                .orElse(null);
        if (resume == null) {
            var resumes = resumeRepository.findByUserId(userId);
            if (resumes.isEmpty()) {
                throw new ResourceNotFoundException("No resume found for user: " + userId);
            }
            resume = resumes.get(0);
        }

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + jobId));

        JobAnalysis analysis = jobAnalysisRepository.findByJobId(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job analysis not found for job: " + jobId));

        String resumeText = resume.getParsedText() != null ? resume.getParsedText() : "No parsed text available";
        String userPrompt = String.format(USER_PROMPT_TEMPLATE,
                resumeText,
                job.getTitle(),
                job.getCompany(),
                job.getDescription() != null ? job.getDescription() : "",
                analysis.getRequiredSkills() != null ? analysis.getRequiredSkills() : "[]",
                analysis.getNiceToHaveSkills() != null ? analysis.getNiceToHaveSkills() : "[]",
                analysis.getKeywords() != null ? analysis.getKeywords() : "[]");

        String model = aiConfig.getTailorModel();
        long startTime = System.currentTimeMillis();

        try {
            JsonNode apiResponse = openAiClient.chatCompletion(SYSTEM_PROMPT, userPrompt, model);
            long durationMs = System.currentTimeMillis() - startTime;

            String content = openAiClient.extractContent(apiResponse);
            OpenAiClient.UsageInfo usage = openAiClient.extractUsage(apiResponse);

            aiUsageLogService.logUsage(user, AiOperationType.RESUME_TAILOR, model,
                    usage.promptTokens(), usage.completionTokens(), usage.totalTokens(),
                    durationMs, true);

            JsonNode parsed = parseJsonContent(content);

            TailoredResume tailored = TailoredResume.builder()
                    .user(user)
                    .resume(resume)
                    .job(job)
                    .tailoredContent(getTextOrDefault(parsed, "tailored_resume_text", content))
                    .modificationsLog(getJsonFieldAsString(parsed, "modifications"))
                    .atsScore(getAtsScore(parsed))
                    .isDownloaded(false)
                    .build();

            TailoredResume saved = tailoredResumeRepository.save(tailored);
            log.info("Tailored resume created: user={}, job={}, atsScore={}", userId, jobId, saved.getAtsScore());
            return saved;

        } catch (AiServiceException e) {
            long durationMs = System.currentTimeMillis() - startTime;
            aiUsageLogService.logFailure(user, AiOperationType.RESUME_TAILOR, model, durationMs);
            throw e;
        }
    }

    private JsonNode parseJsonContent(String content) {
        try {
            String cleaned = content.trim();
            if (cleaned.startsWith("```json")) {
                cleaned = cleaned.substring(7);
            } else if (cleaned.startsWith("```")) {
                cleaned = cleaned.substring(3);
            }
            if (cleaned.endsWith("```")) {
                cleaned = cleaned.substring(0, cleaned.length() - 3);
            }
            return objectMapper.readTree(cleaned.trim());
        } catch (Exception e) {
            throw new AiServiceException("Failed to parse AI tailoring response as JSON: " + e.getMessage(), e);
        }
    }

    private String getTextOrDefault(JsonNode json, String field, String defaultValue) {
        JsonNode node = json.get(field);
        if (node == null || node.isNull()) {
            return defaultValue;
        }
        return node.asText();
    }

    private String getJsonFieldAsString(JsonNode json, String field) {
        JsonNode node = json.get(field);
        if (node == null || node.isNull()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal getAtsScore(JsonNode json) {
        JsonNode node = json.get("estimated_ats_score");
        if (node == null || node.isNull()) {
            return BigDecimal.valueOf(85);
        }
        return BigDecimal.valueOf(node.asDouble());
    }
}
