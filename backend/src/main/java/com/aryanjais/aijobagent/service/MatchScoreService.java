package com.aryanjais.aijobagent.service;

import com.aryanjais.aijobagent.config.MatchingConfig;
import com.aryanjais.aijobagent.entity.Job;
import com.aryanjais.aijobagent.entity.JobAnalysis;
import com.aryanjais.aijobagent.entity.JobMatch;
import com.aryanjais.aijobagent.entity.JobSkill;
import com.aryanjais.aijobagent.entity.Resume;
import com.aryanjais.aijobagent.entity.ResumeSkill;
import com.aryanjais.aijobagent.entity.User;
import com.aryanjais.aijobagent.entity.UserPreference;
import com.aryanjais.aijobagent.entity.enums.MatchStatus;
import com.aryanjais.aijobagent.entity.enums.RemoteType;
import com.aryanjais.aijobagent.exception.ResourceNotFoundException;
import com.aryanjais.aijobagent.repository.JobAnalysisRepository;
import com.aryanjais.aijobagent.repository.JobMatchRepository;
import com.aryanjais.aijobagent.repository.JobRepository;
import com.aryanjais.aijobagent.repository.JobSkillRepository;
import com.aryanjais.aijobagent.repository.ResumeRepository;
import com.aryanjais.aijobagent.repository.ResumeSkillRepository;
import com.aryanjais.aijobagent.repository.UserPreferenceRepository;
import com.aryanjais.aijobagent.repository.UserRepository;
import com.aryanjais.aijobagent.util.SkillNormalizer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Weighted Match Score computation engine (T-3.6, T-3.7).
 * Weights: Skill 50% + Experience 25% + Location 15% + Domain 10%.
 */
@Service
@RequiredArgsConstructor
public class MatchScoreService {

    private static final Logger log = LoggerFactory.getLogger(MatchScoreService.class);

    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;
    private final ResumeSkillRepository resumeSkillRepository;
    private final JobRepository jobRepository;
    private final JobAnalysisRepository jobAnalysisRepository;
    private final JobSkillRepository jobSkillRepository;
    private final JobMatchRepository jobMatchRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final MatchingConfig matchingConfig;
    private final ObjectMapper objectMapper;

    private static final double SKILL_WEIGHT = 0.50;
    private static final double EXPERIENCE_WEIGHT = 0.25;
    private static final double LOCATION_WEIGHT = 0.15;
    private static final double DOMAIN_WEIGHT = 0.10;

    /**
     * Compute match score for a specific user-job pair and persist the result.
     *
     * @param userId The user ID
     * @param jobId  The job ID
     * @return The persisted JobMatch, or null if score is below threshold
     */
    @Transactional
    public JobMatch computeAndPersistMatch(Long userId, Long jobId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Resume resume = resumeRepository.findByUserIdAndIsPrimary(userId, true)
                .orElse(null);
        if (resume == null) {
            List<Resume> resumes = resumeRepository.findByUserId(userId);
            if (resumes.isEmpty()) {
                log.debug("User {} has no resumes, skipping match for job {}", userId, jobId);
                return null;
            }
            resume = resumes.get(0);
        }

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + jobId));

        Optional<JobAnalysis> analysisOpt = jobAnalysisRepository.findByJobId(jobId);
        if (analysisOpt.isEmpty()) {
            log.debug("Job {} not yet analysed, skipping match", jobId);
            return null;
        }
        JobAnalysis analysis = analysisOpt.get();

        Optional<UserPreference> preferencesOpt = userPreferenceRepository.findByUserId(userId);

        // Compute individual scores
        double skillScore = computeSkillScore(resume, job);
        double experienceScore = computeExperienceScore(user, analysis);
        double locationScore = computeLocationScore(user, job, analysis, preferencesOpt.orElse(null));
        double domainScore = computeDomainScore(analysis, preferencesOpt.orElse(null));

        // Weighted overall score
        double overallScore = skillScore * SKILL_WEIGHT
                + experienceScore * EXPERIENCE_WEIGHT
                + locationScore * LOCATION_WEIGHT
                + domainScore * DOMAIN_WEIGHT;

        overallScore = Math.round(overallScore * 100.0) / 100.0;

        if (overallScore < matchingConfig.getMinimumScoreThreshold()) {
            log.debug("Match score {:.2f} below threshold for user {} job {}", overallScore, userId, jobId);
            return null;
        }

        // Persist the match
        JobMatch match = JobMatch.builder()
                .user(user)
                .resume(resume)
                .job(job)
                .overallScore(BigDecimal.valueOf(overallScore).setScale(2, RoundingMode.HALF_UP))
                .skillScore(BigDecimal.valueOf(skillScore).setScale(2, RoundingMode.HALF_UP))
                .experienceScore(BigDecimal.valueOf(experienceScore).setScale(2, RoundingMode.HALF_UP))
                .locationScore(BigDecimal.valueOf(locationScore).setScale(2, RoundingMode.HALF_UP))
                .domainScore(BigDecimal.valueOf(domainScore).setScale(2, RoundingMode.HALF_UP))
                .status(MatchStatus.NEW)
                .build();

        JobMatch saved = jobMatchRepository.save(match);
        log.info("Match persisted: user={}, job={}, score={}", userId, jobId, overallScore);
        return saved;
    }

    /**
     * Compute match scores for all users against a specific job.
     */
    @Transactional
    public List<JobMatch> computeMatchesForJob(Long jobId) {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> computeAndPersistMatch(user.getId(), jobId))
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Compute skill score (50% weight).
     * Based on overlap between resume skills and job required/optional skills.
     */
    double computeSkillScore(Resume resume, Job job) {
        List<ResumeSkill> resumeSkills = resumeSkillRepository.findByResumeId(resume.getId());
        List<JobSkill> jobSkills = jobSkillRepository.findByJobId(job.getId());

        Set<String> resumeSkillNames = resumeSkills.stream()
                .map(s -> SkillNormalizer.normalize(s.getSkillName()).toLowerCase())
                .collect(Collectors.toSet());

        Set<String> requiredSkills = jobSkills.stream()
                .filter(JobSkill::getIsRequired)
                .map(s -> SkillNormalizer.normalize(s.getSkillName()).toLowerCase())
                .collect(Collectors.toSet());

        Set<String> optionalSkills = jobSkills.stream()
                .filter(s -> !s.getIsRequired())
                .map(s -> SkillNormalizer.normalize(s.getSkillName()).toLowerCase())
                .collect(Collectors.toSet());

        if (requiredSkills.isEmpty()) {
            return 50.0; // No required skills → neutral score
        }

        Set<String> matchedRequired = new HashSet<>(requiredSkills);
        matchedRequired.retainAll(resumeSkillNames);

        Set<String> matchedOptional = new HashSet<>(optionalSkills);
        matchedOptional.retainAll(resumeSkillNames);

        double baseScore = ((double) matchedRequired.size() / requiredSkills.size()) * 100.0;
        double bonus = Math.min(10.0, matchedOptional.size() * 2.0);
        return Math.min(100.0, baseScore + bonus);
    }

    /**
     * Compute experience score (25% weight).
     * Perfect score if user experience is within the required range.
     */
    double computeExperienceScore(User user, JobAnalysis analysis) {
        int userYears = user.getExperienceYears() != null ? user.getExperienceYears() : 0;
        int expMin = analysis.getExperienceMin() != null ? analysis.getExperienceMin() : 0;
        int expMax = analysis.getExperienceMax() != null ? analysis.getExperienceMax() : 99;

        if (userYears >= expMin && userYears <= expMax + 2) {
            return 100.0;
        } else if (userYears < expMin) {
            double gap = expMin - userYears;
            return Math.max(0.0, 100.0 - (gap * 20.0));
        } else {
            // Overqualified
            return 80.0;
        }
    }

    /**
     * Compute location score (15% weight).
     * Full score for remote jobs or matching cities.
     */
    double computeLocationScore(User user, Job job, JobAnalysis analysis, UserPreference preferences) {
        if (analysis.getRemoteType() == RemoteType.REMOTE) {
            return 100.0;
        }

        String userLocation = normalizeCity(user.getLocation());
        String jobLocation = normalizeCity(job.getLocation());

        if (!userLocation.isEmpty() && !jobLocation.isEmpty() && userLocation.equals(jobLocation)) {
            return 100.0;
        }

        if (preferences != null && preferences.getRemoteOk() != null && preferences.getRemoteOk()
                && analysis.getRemoteType() == RemoteType.HYBRID) {
            return 80.0;
        }

        // Check if locations share a state/region
        if (!userLocation.isEmpty() && !jobLocation.isEmpty() && sameRegion(userLocation, jobLocation)) {
            return 60.0;
        }

        return 0.0;
    }

    /**
     * Compute domain score (10% weight).
     * Full score if job domain matches user's preferred industries.
     */
    double computeDomainScore(JobAnalysis analysis, UserPreference preferences) {
        if (analysis.getDomain() == null || analysis.getDomain().isBlank()) {
            return 30.0; // Unknown domain
        }

        if (preferences != null && preferences.getIndustries() != null) {
            Set<String> industries = parseJsonList(preferences.getIndustries()).stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());
            if (industries.contains(analysis.getDomain().toLowerCase())) {
                return 100.0;
            }
        }

        // Default partial score
        return 30.0;
    }

    private String normalizeCity(String location) {
        if (location == null || location.isBlank()) {
            return "";
        }
        // Take the first part before comma (city name)
        String city = location.split(",")[0].trim().toLowerCase();
        // Normalize common variants
        city = city.replace("bengaluru", "bangalore")
                .replace("bombay", "mumbai")
                .replace("madras", "chennai")
                .replace("calcutta", "kolkata")
                .replace("new delhi", "delhi")
                .replace("noida", "delhi ncr")
                .replace("gurgaon", "delhi ncr")
                .replace("gurugram", "delhi ncr");
        return city;
    }

    private boolean sameRegion(String city1, String city2) {
        // Delhi NCR region
        Set<String> delhiNcr = Set.of("delhi", "delhi ncr", "noida", "gurgaon", "gurugram", "faridabad", "ghaziabad");
        if (delhiNcr.contains(city1) && delhiNcr.contains(city2)) {
            return true;
        }
        // Same state approximation based on major cities
        return false;
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
