package com.aryanjais.aijobagent.service;

import com.aryanjais.aijobagent.config.AiConfig;
import com.aryanjais.aijobagent.entity.Job;
import com.aryanjais.aijobagent.entity.JobAnalysis;
import com.aryanjais.aijobagent.entity.JobSkill;
import com.aryanjais.aijobagent.entity.enums.AiOperationType;
import com.aryanjais.aijobagent.entity.enums.RemoteType;
import com.aryanjais.aijobagent.entity.enums.SeniorityLevel;
import com.aryanjais.aijobagent.entity.enums.SkillCategory;
import com.aryanjais.aijobagent.exception.AiServiceException;
import com.aryanjais.aijobagent.repository.JobAnalysisRepository;
import com.aryanjais.aijobagent.repository.JobRepository;
import com.aryanjais.aijobagent.repository.JobSkillRepository;
import com.aryanjais.aijobagent.util.SkillNormalizer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for AI-powered Job Description analysis (T-3.2, T-3.3).
 * Sends JD text to OpenAI, parses the structured response,
 * and persists to job_analyses and job_skills tables.
 */
@Service
@RequiredArgsConstructor
public class JobAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(JobAnalysisService.class);

    private final OpenAiClient openAiClient;
    private final AiConfig aiConfig;
    private final AiUsageLogService aiUsageLogService;
    private final JobRepository jobRepository;
    private final JobAnalysisRepository jobAnalysisRepository;
    private final JobSkillRepository jobSkillRepository;
    private final ObjectMapper objectMapper;

    private static final String SYSTEM_PROMPT = """
            You are an expert technical recruiter and ATS specialist. Analyse the following job description \
            and extract structured information. Respond ONLY with valid JSON matching the schema below.""";

    private static final String USER_PROMPT_TEMPLATE = """
            Analyse this job description and return structured JSON:
            
            Job Description:
            ---
            %s
            ---
            
            Return JSON with this exact schema:
            {
              "required_skills": ["string"],
              "nice_to_have_skills": ["string"],
              "experience_min": number,
              "experience_max": number,
              "education_required": "string",
              "domain": "string",
              "keywords": ["string"],
              "seniority_level": "INTERN|JUNIOR|MID|SENIOR|LEAD|MANAGER|DIRECTOR",
              "remote_type": "REMOTE|HYBRID|ON_SITE"
            }
            
            Rules:
            - If a value cannot be determined, use null
            - experience_min defaults to 0 if not specified
            - Normalise skill names (e.g., "node.js" -> "Node.js", "springboot" -> "Spring Boot")
            - Return ONLY the JSON object, no markdown, no explanation""";

    /**
     * Analyse a job description using AI and persist the results.
     *
     * @param jobId The ID of the job to analyse
     * @return The persisted JobAnalysis entity
     */
    @Transactional
    public JobAnalysis analyzeJob(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new AiServiceException("Job not found with id: " + jobId));

        // Skip if already analysed
        Optional<JobAnalysis> existing = jobAnalysisRepository.findByJobId(jobId);
        if (existing.isPresent()) {
            log.debug("Job {} already analysed, skipping", jobId);
            return existing.get();
        }

        String jdText = buildJdText(job);
        String userPrompt = String.format(USER_PROMPT_TEMPLATE, jdText);
        String model = aiConfig.getAnalysisModel();

        long startTime = System.currentTimeMillis();
        try {
            JsonNode apiResponse = openAiClient.chatCompletion(SYSTEM_PROMPT, userPrompt, model);
            long durationMs = System.currentTimeMillis() - startTime;

            String content = openAiClient.extractContent(apiResponse);
            OpenAiClient.UsageInfo usage = openAiClient.extractUsage(apiResponse);

            // Log usage
            aiUsageLogService.logUsage(null, AiOperationType.JD_ANALYSIS, model,
                    usage.promptTokens(), usage.completionTokens(), usage.totalTokens(),
                    durationMs, true);

            // Parse and persist
            JsonNode analysisJson = parseJsonContent(content);
            JobAnalysis analysis = persistAnalysis(job, analysisJson);
            persistJobSkills(job, analysisJson);

            log.info("Job {} analysed successfully in {}ms", jobId, durationMs);
            return analysis;
        } catch (AiServiceException e) {
            long durationMs = System.currentTimeMillis() - startTime;
            aiUsageLogService.logFailure(null, AiOperationType.JD_ANALYSIS, model, durationMs);
            throw e;
        }
    }

    private String buildJdText(Job job) {
        StringBuilder sb = new StringBuilder();
        sb.append("Title: ").append(job.getTitle()).append("\n");
        sb.append("Company: ").append(job.getCompany()).append("\n");
        if (job.getLocation() != null) {
            sb.append("Location: ").append(job.getLocation()).append("\n");
        }
        if (job.getExperienceRequired() != null) {
            sb.append("Experience: ").append(job.getExperienceRequired()).append("\n");
        }
        if (job.getDescription() != null) {
            sb.append("\n").append(job.getDescription());
        }
        if (job.getRequirements() != null) {
            sb.append("\n\nRequirements:\n").append(job.getRequirements());
        }
        return sb.toString();
    }

    private JsonNode parseJsonContent(String content) {
        try {
            // Strip markdown code fences if present
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
            throw new AiServiceException("Failed to parse AI response as JSON: " + e.getMessage(), e);
        }
    }

    private JobAnalysis persistAnalysis(Job job, JsonNode json) {
        JobAnalysis analysis = JobAnalysis.builder()
                .job(job)
                .requiredSkills(getJsonArrayText(json, "required_skills"))
                .niceToHaveSkills(getJsonArrayText(json, "nice_to_have_skills"))
                .experienceMin(getIntOrNull(json, "experience_min"))
                .experienceMax(getIntOrNull(json, "experience_max"))
                .educationRequired(getTextOrNull(json, "education_required"))
                .domain(getTextOrNull(json, "domain"))
                .keywords(getJsonArrayText(json, "keywords"))
                .seniorityLevel(parseSeniorityLevel(getTextOrNull(json, "seniority_level")))
                .remoteType(parseRemoteType(getTextOrNull(json, "remote_type")))
                .analyzedAt(LocalDateTime.now())
                .build();

        return jobAnalysisRepository.save(analysis);
    }

    private void persistJobSkills(Job job, JsonNode json) {
        List<JobSkill> skills = new ArrayList<>();

        // Required skills
        JsonNode requiredNode = json.get("required_skills");
        if (requiredNode != null && requiredNode.isArray()) {
            int importance = 80;
            for (JsonNode skillNode : requiredNode) {
                String skillName = SkillNormalizer.normalize(skillNode.asText());
                if (!skillName.isEmpty()) {
                    skills.add(JobSkill.builder()
                            .job(job)
                            .skillName(skillName)
                            .skillCategory(SkillCategory.TECHNICAL)
                            .isRequired(true)
                            .importanceScore(Math.max(50, importance))
                            .build());
                    importance -= 5;
                }
            }
        }

        // Nice-to-have skills
        JsonNode optionalNode = json.get("nice_to_have_skills");
        if (optionalNode != null && optionalNode.isArray()) {
            for (JsonNode skillNode : optionalNode) {
                String skillName = SkillNormalizer.normalize(skillNode.asText());
                if (!skillName.isEmpty()) {
                    skills.add(JobSkill.builder()
                            .job(job)
                            .skillName(skillName)
                            .skillCategory(SkillCategory.TECHNICAL)
                            .isRequired(false)
                            .importanceScore(30)
                            .build());
                }
            }
        }

        if (!skills.isEmpty()) {
            jobSkillRepository.saveAll(skills);
        }
    }

    private String getJsonArrayText(JsonNode json, String field) {
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

    private String getTextOrNull(JsonNode json, String field) {
        JsonNode node = json.get(field);
        if (node == null || node.isNull()) {
            return null;
        }
        return node.asText();
    }

    private Integer getIntOrNull(JsonNode json, String field) {
        JsonNode node = json.get(field);
        if (node == null || node.isNull()) {
            return null;
        }
        return node.asInt();
    }

    private SeniorityLevel parseSeniorityLevel(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return SeniorityLevel.valueOf(value.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown seniority_level '{}', ignoring", value);
            return null;
        }
    }

    private RemoteType parseRemoteType(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return RemoteType.valueOf(value.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown remote_type '{}', ignoring", value);
            return null;
        }
    }
}
