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
import com.aryanjais.aijobagent.entity.enums.RemoteType;
import com.aryanjais.aijobagent.entity.enums.SkillCategory;
import com.aryanjais.aijobagent.repository.JobAnalysisRepository;
import com.aryanjais.aijobagent.repository.JobMatchRepository;
import com.aryanjais.aijobagent.repository.JobRepository;
import com.aryanjais.aijobagent.repository.JobSkillRepository;
import com.aryanjais.aijobagent.repository.ResumeRepository;
import com.aryanjais.aijobagent.repository.ResumeSkillRepository;
import com.aryanjais.aijobagent.repository.UserPreferenceRepository;
import com.aryanjais.aijobagent.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchScoreServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private ResumeRepository resumeRepository;
    @Mock private ResumeSkillRepository resumeSkillRepository;
    @Mock private JobRepository jobRepository;
    @Mock private JobAnalysisRepository jobAnalysisRepository;
    @Mock private JobSkillRepository jobSkillRepository;
    @Mock private JobMatchRepository jobMatchRepository;
    @Mock private UserPreferenceRepository userPreferenceRepository;
    @Spy private MatchingConfig matchingConfig = new MatchingConfig();
    @Spy private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private MatchScoreService matchScoreService;

    private User testUser;
    private Resume testResume;
    private Job testJob;
    private JobAnalysis testAnalysis;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).experienceYears(5).location("Bangalore, India").build();
        testResume = Resume.builder().id(10L).user(testUser).isPrimary(true).build();
        testJob = Job.builder().id(100L).title("Java Developer").company("TechCo").location("Bangalore").build();
        testAnalysis = JobAnalysis.builder()
                .id(1000L).job(testJob)
                .experienceMin(3).experienceMax(7)
                .remoteType(RemoteType.ON_SITE)
                .domain("FinTech")
                .build();
    }

    @Test
    void computeSkillScore_perfectMatch_returns100() {
        List<ResumeSkill> resumeSkills = List.of(
                buildResumeSkill("Java"), buildResumeSkill("Spring Boot"), buildResumeSkill("Docker"));
        List<JobSkill> jobSkills = List.of(
                buildJobSkill("Java", true), buildJobSkill("Spring Boot", true));

        when(resumeSkillRepository.findByResumeId(10L)).thenReturn(resumeSkills);
        when(jobSkillRepository.findByJobId(100L)).thenReturn(jobSkills);

        double score = matchScoreService.computeSkillScore(testResume, testJob);
        assertEquals(100.0, score, 0.01);
    }

    @Test
    void computeSkillScore_noRequiredSkills_returns50() {
        when(resumeSkillRepository.findByResumeId(10L)).thenReturn(List.of());
        when(jobSkillRepository.findByJobId(100L)).thenReturn(List.of());

        double score = matchScoreService.computeSkillScore(testResume, testJob);
        assertEquals(50.0, score, 0.01);
    }

    @Test
    void computeSkillScore_partialMatch_returnsProportional() {
        List<ResumeSkill> resumeSkills = List.of(buildResumeSkill("Java"));
        List<JobSkill> jobSkills = List.of(
                buildJobSkill("Java", true), buildJobSkill("Spring Boot", true));

        when(resumeSkillRepository.findByResumeId(10L)).thenReturn(resumeSkills);
        when(jobSkillRepository.findByJobId(100L)).thenReturn(jobSkills);

        double score = matchScoreService.computeSkillScore(testResume, testJob);
        assertEquals(50.0, score, 0.01); // 1 of 2 required = 50%
    }

    @Test
    void computeSkillScore_withOptionalBonus_capAt100() {
        List<ResumeSkill> resumeSkills = List.of(
                buildResumeSkill("Java"), buildResumeSkill("Spring Boot"),
                buildResumeSkill("Docker"), buildResumeSkill("Kubernetes"));
        List<JobSkill> jobSkills = List.of(
                buildJobSkill("Java", true), buildJobSkill("Spring Boot", true),
                buildJobSkill("Docker", false), buildJobSkill("Kubernetes", false));

        when(resumeSkillRepository.findByResumeId(10L)).thenReturn(resumeSkills);
        when(jobSkillRepository.findByJobId(100L)).thenReturn(jobSkills);

        double score = matchScoreService.computeSkillScore(testResume, testJob);
        assertEquals(100.0, score, 0.01); // 100 base + 4 bonus, capped at 100
    }

    @Test
    void computeExperienceScore_inRange_returns100() {
        double score = matchScoreService.computeExperienceScore(testUser, testAnalysis);
        assertEquals(100.0, score, 0.01);
    }

    @Test
    void computeExperienceScore_underQualified_penalised() {
        User junior = User.builder().id(2L).experienceYears(1).build();
        double score = matchScoreService.computeExperienceScore(junior, testAnalysis);
        assertEquals(60.0, score, 0.01); // gap = 2, penalty = 40
    }

    @Test
    void computeExperienceScore_overQualified_returns80() {
        User senior = User.builder().id(3L).experienceYears(15).build();
        double score = matchScoreService.computeExperienceScore(senior, testAnalysis);
        assertEquals(80.0, score, 0.01);
    }

    @Test
    void computeLocationScore_remoteJob_returns100() {
        JobAnalysis remoteAnalysis = JobAnalysis.builder().remoteType(RemoteType.REMOTE).build();
        double score = matchScoreService.computeLocationScore(testUser, testJob, remoteAnalysis, null);
        assertEquals(100.0, score, 0.01);
    }

    @Test
    void computeLocationScore_sameCity_returns100() {
        double score = matchScoreService.computeLocationScore(testUser, testJob, testAnalysis, null);
        assertEquals(100.0, score, 0.01);
    }

    @Test
    void computeLocationScore_differentCity_returns0() {
        User delhi = User.builder().id(4L).location("Chennai").build();
        Job mumbaiJob = Job.builder().id(200L).location("Mumbai").build();
        double score = matchScoreService.computeLocationScore(delhi, mumbaiJob, testAnalysis, null);
        assertEquals(0.0, score, 0.01);
    }

    @Test
    void computeDomainScore_matchingIndustry_returns100() {
        UserPreference prefs = UserPreference.builder().industries("[\"FinTech\",\"Banking\"]").build();
        double score = matchScoreService.computeDomainScore(testAnalysis, prefs);
        assertEquals(100.0, score, 0.01);
    }

    @Test
    void computeDomainScore_noPreferences_returns30() {
        double score = matchScoreService.computeDomainScore(testAnalysis, null);
        assertEquals(30.0, score, 0.01);
    }

    @Test
    void computeAndPersistMatch_belowThreshold_returnsNull() {
        // User with 0 experience, no matching skills = very low score
        User noExpUser = User.builder().id(5L).experienceYears(0).location("Remote").build();
        Resume noSkillResume = Resume.builder().id(50L).user(noExpUser).isPrimary(true).build();

        when(userRepository.findById(5L)).thenReturn(Optional.of(noExpUser));
        when(resumeRepository.findByUserIdAndIsPrimary(5L, true)).thenReturn(Optional.of(noSkillResume));
        when(jobRepository.findById(100L)).thenReturn(Optional.of(testJob));
        when(jobAnalysisRepository.findByJobId(100L)).thenReturn(Optional.of(testAnalysis));
        when(userPreferenceRepository.findByUserId(5L)).thenReturn(Optional.empty());
        when(resumeSkillRepository.findByResumeId(50L)).thenReturn(List.of());
        when(jobSkillRepository.findByJobId(100L)).thenReturn(List.of(
                buildJobSkill("Java", true), buildJobSkill("Spring Boot", true)));

        JobMatch result = matchScoreService.computeAndPersistMatch(5L, 100L);
        assertNull(result);
    }

    @Test
    void computeAndPersistMatch_aboveThreshold_persistsMatch() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(resumeRepository.findByUserIdAndIsPrimary(1L, true)).thenReturn(Optional.of(testResume));
        when(jobRepository.findById(100L)).thenReturn(Optional.of(testJob));
        when(jobAnalysisRepository.findByJobId(100L)).thenReturn(Optional.of(testAnalysis));
        when(userPreferenceRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(resumeSkillRepository.findByResumeId(10L)).thenReturn(List.of(
                buildResumeSkill("Java"), buildResumeSkill("Spring Boot")));
        when(jobSkillRepository.findByJobId(100L)).thenReturn(List.of(
                buildJobSkill("Java", true), buildJobSkill("Spring Boot", true)));
        when(jobMatchRepository.save(any(JobMatch.class))).thenAnswer(inv -> {
            JobMatch m = inv.getArgument(0);
            m.setId(1L);
            return m;
        });

        JobMatch result = matchScoreService.computeAndPersistMatch(1L, 100L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    private ResumeSkill buildResumeSkill(String name) {
        return ResumeSkill.builder()
                .resume(testResume)
                .skillName(name)
                .skillCategory(SkillCategory.TECHNICAL)
                .build();
    }

    private JobSkill buildJobSkill(String name, boolean required) {
        return JobSkill.builder()
                .job(testJob)
                .skillName(name)
                .skillCategory(SkillCategory.TECHNICAL)
                .isRequired(required)
                .importanceScore(80)
                .build();
    }
}
