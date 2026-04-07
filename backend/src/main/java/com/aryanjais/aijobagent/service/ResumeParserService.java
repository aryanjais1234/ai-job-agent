package com.aryanjais.aijobagent.service;

import com.aryanjais.aijobagent.entity.Resume;
import com.aryanjais.aijobagent.entity.ResumeEducation;
import com.aryanjais.aijobagent.entity.ResumeExperience;
import com.aryanjais.aijobagent.entity.ResumeSkill;
import com.aryanjais.aijobagent.entity.enums.ProficiencyLevel;
import com.aryanjais.aijobagent.entity.enums.SkillCategory;
import com.aryanjais.aijobagent.exception.FileStorageException;
import com.aryanjais.aijobagent.exception.ResourceNotFoundException;
import com.aryanjais.aijobagent.repository.ResumeEducationRepository;
import com.aryanjais.aijobagent.repository.ResumeExperienceRepository;
import com.aryanjais.aijobagent.repository.ResumeRepository;
import com.aryanjais.aijobagent.repository.ResumeSkillRepository;
import com.aryanjais.aijobagent.util.SkillNormalizer;
import com.aryanjais.aijobagent.util.TextSanitizer;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for parsing resume files using Apache Tika and extracting structured data (T-3.4, T-3.5).
 * Populates resume_skills, resume_experiences, resume_educations tables.
 */
@Service
@RequiredArgsConstructor
public class ResumeParserService {

    private static final Logger log = LoggerFactory.getLogger(ResumeParserService.class);

    private final ResumeRepository resumeRepository;
    private final ResumeSkillRepository resumeSkillRepository;
    private final ResumeExperienceRepository resumeExperienceRepository;
    private final ResumeEducationRepository resumeEducationRepository;

    private final Tika tika = new Tika();

    // Common technical skills to detect in resume text
    private static final Set<String> KNOWN_SKILLS = Set.of(
            "Java", "Python", "JavaScript", "TypeScript", "C++", "C#", "Go", "Rust", "Ruby", "PHP",
            "Kotlin", "Swift", "Scala", "R", "MATLAB", "Perl", "Shell",
            "Spring Boot", "Spring", "Django", "Flask", "FastAPI", "Express", "Node.js",
            "React", "Angular", "Vue.js", "Next.js", "Svelte",
            "MySQL", "PostgreSQL", "MongoDB", "Redis", "Cassandra", "DynamoDB", "Elasticsearch",
            "Docker", "Kubernetes", "AWS", "Azure", "GCP", "Terraform", "Jenkins", "GitHub Actions",
            "Kafka", "RabbitMQ", "GraphQL", "REST APIs", "gRPC",
            "Machine Learning", "Deep Learning", "TensorFlow", "PyTorch", "NLP",
            "Git", "Linux", "Nginx", "Apache", "Microservices", "CI/CD",
            "HTML", "CSS", "Sass", "Tailwind", "Bootstrap",
            "JUnit", "Mockito", "Selenium", "Cypress", "Jest",
            "Hadoop", "Spark", "Airflow", "Tableau", "Power BI",
            "Figma", "Jira", "Confluence", "Agile", "Scrum",
            ".NET", "ASP.NET", "Entity Framework", "Hibernate", "JPA",
            "SQL", "NoSQL", "Data Structures", "Algorithms", "System Design",
            "OAuth", "JWT", "LDAP", "SSO"
    );

    // Patterns for experience section detection
    private static final Pattern EXPERIENCE_PATTERN = Pattern.compile(
            "(?i)(?:^|\\n)\\s*(?:work\\s+)?experience\\s*(?::|\\n)",
            Pattern.MULTILINE);

    // Pattern matches: "Title at/- Company StartDate - EndDate"
    // Group 1: Job title, Group 2: Company name, Group 3: Start date (year or month+year), Group 4: End date (year, month+year, "Present", or "Current")
    private static final Pattern JOB_ENTRY_PATTERN = Pattern.compile(
            "(?i)([A-Z][A-Za-z\\s&.,'-]+?)\\s*(?:[-|–—@at]|\\bat\\b)\\s*([A-Z][A-Za-z\\s&.,'-]+?)\\s*" +
                    "(?:[-|–—]|\\bfrom\\b)?\\s*(?:(\\d{4}|(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\\s*\\d{4})" +
                    "\\s*[-–—to]+\\s*(\\d{4}|(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\\s*\\d{4}|Present|Current))?",
            Pattern.MULTILINE);

    // Patterns for education section detection
    private static final Pattern EDUCATION_PATTERN = Pattern.compile(
            "(?i)(?:^|\\n)\\s*education\\s*(?::|\\n)",
            Pattern.MULTILINE);

    private static final Pattern DEGREE_PATTERN = Pattern.compile(
            "(?i)(B\\.?(?:Tech|E|S|Sc|A|Com)|M\\.?(?:Tech|S|Sc|E|A|Com|BA)|Ph\\.?D|MBA|" +
                    "Bachelor(?:'s)?|Master(?:'s)?|Diploma)\\s*(?:in|of)?\\s*([A-Za-z\\s,]+?)?" +
                    "\\s*(?:from|[-–—,@at]|\\bat\\b)?\\s*([A-Z][A-Za-z\\s&.,'-]+?)?" +
                    "\\s*(?:[-–—,])?\\s*(\\d{4})?",
            Pattern.MULTILINE);

    /**
     * Parse a resume file, extract text, and populate related tables.
     *
     * @param resumeId The ID of the resume to parse
     * @return The updated Resume entity
     */
    @Transactional
    public Resume parseResume(Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with id: " + resumeId));

        String parsedText = extractText(resume.getFilePath());
        String sanitizedText = TextSanitizer.sanitize(parsedText);

        resume.setParsedText(sanitizedText);

        // Extract and persist skills
        List<ResumeSkill> skills = extractSkills(resume, sanitizedText);
        resumeSkillRepository.saveAll(skills);

        // Build skills JSON summary
        List<String> skillNames = skills.stream().map(ResumeSkill::getSkillName).toList();
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            resume.setSkillsJson(mapper.writeValueAsString(skillNames));
        } catch (Exception e) {
            log.warn("Failed to serialize skills JSON: {}", e.getMessage());
        }

        // Extract and persist experiences
        List<ResumeExperience> experiences = extractExperiences(resume, sanitizedText);
        if (!experiences.isEmpty()) {
            resumeExperienceRepository.saveAll(experiences);
            resume.setExperienceSummary(buildExperienceSummary(experiences));
        }

        // Extract and persist educations
        List<ResumeEducation> educations = extractEducations(resume, sanitizedText);
        if (!educations.isEmpty()) {
            resumeEducationRepository.saveAll(educations);
            resume.setEducationSummary(buildEducationSummary(educations));
        }

        return resumeRepository.save(resume);
    }

    /**
     * Extract text content from a file using Apache Tika.
     */
    String extractText(String filePath) {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new FileStorageException("Resume file not found at: " + filePath);
        }

        try (InputStream is = Files.newInputStream(path)) {
            String text = tika.parseToString(is);
            log.debug("Extracted {} characters from {}", text.length(), path.getFileName());
            return text;
        } catch (IOException | TikaException e) {
            throw new FileStorageException("Failed to parse resume file: " + e.getMessage(), e);
        }
    }

    /**
     * Extract skills from resume text by matching against known skill set.
     */
    List<ResumeSkill> extractSkills(Resume resume, String text) {
        Set<String> found = new LinkedHashSet<>();
        String textLower = text.toLowerCase();

        for (String skill : KNOWN_SKILLS) {
            String skillLower = skill.toLowerCase();
            // Use word boundary check to avoid partial matches
            if (containsSkill(textLower, skillLower)) {
                found.add(SkillNormalizer.normalize(skill));
            }
        }

        List<ResumeSkill> skills = new ArrayList<>();
        for (String skillName : found) {
            skills.add(ResumeSkill.builder()
                    .resume(resume)
                    .skillName(skillName)
                    .skillCategory(categorizeSkill(skillName))
                    .proficiencyLevel(ProficiencyLevel.INTERMEDIATE)
                    .build());
        }

        log.debug("Extracted {} skills from resume {}", skills.size(), resume.getId());
        return skills;
    }

    /**
     * Extract work experiences from resume text.
     */
    List<ResumeExperience> extractExperiences(Resume resume, String text) {
        List<ResumeExperience> experiences = new ArrayList<>();

        Matcher expMatcher = EXPERIENCE_PATTERN.matcher(text);
        if (!expMatcher.find()) {
            return experiences;
        }

        String experienceSection = text.substring(expMatcher.start());
        // Limit to the experience section (cut at next major section)
        Matcher nextSection = Pattern.compile("(?i)(?:^|\\n)\\s*(?:education|projects?|certifications?|skills)\\s*(?::|\\n)")
                .matcher(experienceSection);
        if (nextSection.find()) {
            experienceSection = experienceSection.substring(0, nextSection.start());
        }

        Matcher jobMatcher = JOB_ENTRY_PATTERN.matcher(experienceSection);
        while (jobMatcher.find() && experiences.size() < 10) {
            String title = jobMatcher.group(1) != null ? jobMatcher.group(1).trim() : "Unknown";
            String company = jobMatcher.group(2) != null ? jobMatcher.group(2).trim() : "Unknown";
            String startStr = jobMatcher.group(3);
            String endStr = jobMatcher.group(4);

            boolean isCurrent = endStr != null &&
                    (endStr.equalsIgnoreCase("Present") || endStr.equalsIgnoreCase("Current"));

            experiences.add(ResumeExperience.builder()
                    .resume(resume)
                    .title(TextSanitizer.truncate(title, 255))
                    .company(TextSanitizer.truncate(company, 255))
                    .startDate(parseYear(startStr))
                    .endDate(isCurrent ? null : parseYear(endStr))
                    .isCurrent(isCurrent)
                    .build());
        }

        log.debug("Extracted {} experiences from resume {}", experiences.size(), resume.getId());
        return experiences;
    }

    /**
     * Extract education entries from resume text.
     */
    List<ResumeEducation> extractEducations(Resume resume, String text) {
        List<ResumeEducation> educations = new ArrayList<>();

        Matcher eduMatcher = EDUCATION_PATTERN.matcher(text);
        if (!eduMatcher.find()) {
            return educations;
        }

        String educationSection = text.substring(eduMatcher.start());
        Matcher nextSection = Pattern.compile("(?i)(?:^|\\n)\\s*(?:experience|projects?|certifications?|skills)\\s*(?::|\\n)")
                .matcher(educationSection);
        if (nextSection.find()) {
            educationSection = educationSection.substring(0, nextSection.start());
        }

        Matcher degreeMatcher = DEGREE_PATTERN.matcher(educationSection);
        while (degreeMatcher.find() && educations.size() < 5) {
            String degree = degreeMatcher.group(1) != null ? degreeMatcher.group(1).trim() : null;
            String field = degreeMatcher.group(2) != null ? degreeMatcher.group(2).trim() : null;
            String institution = degreeMatcher.group(3) != null ? degreeMatcher.group(3).trim() : null;
            String yearStr = degreeMatcher.group(4);

            Integer gradYear = null;
            if (yearStr != null) {
                try {
                    gradYear = Integer.parseInt(yearStr.trim());
                } catch (NumberFormatException ignored) {
                }
            }

            educations.add(ResumeEducation.builder()
                    .resume(resume)
                    .degree(TextSanitizer.truncate(degree, 100))
                    .fieldOfStudy(TextSanitizer.truncate(field, 255))
                    .institution(TextSanitizer.truncate(institution != null ? institution : "Unknown", 255))
                    .graduationYear(gradYear)
                    .build());
        }

        log.debug("Extracted {} educations from resume {}", educations.size(), resume.getId());
        return educations;
    }

    private boolean containsSkill(String textLower, String skillLower) {
        int idx = textLower.indexOf(skillLower);
        if (idx < 0) {
            return false;
        }
        // Check word boundaries
        boolean startOk = idx == 0 || !Character.isLetterOrDigit(textLower.charAt(idx - 1));
        int endIdx = idx + skillLower.length();
        boolean endOk = endIdx >= textLower.length() || !Character.isLetterOrDigit(textLower.charAt(endIdx));
        return startOk && endOk;
    }

    private SkillCategory categorizeSkill(String skillName) {
        Set<String> languages = Set.of("Java", "Python", "JavaScript", "TypeScript", "C++", "C#",
                "Go", "Rust", "Ruby", "PHP", "Kotlin", "Swift", "Scala", "R", "MATLAB", "Perl", "Shell",
                "HTML", "CSS", "SQL");
        Set<String> tools = Set.of("Docker", "Kubernetes", "AWS", "Azure", "GCP", "Terraform",
                "Jenkins", "GitHub Actions", "Git", "Linux", "Nginx", "Apache", "Jira", "Confluence",
                "Figma", "Tableau", "Power BI", "Hadoop", "Spark", "Airflow");
        Set<String> softSkills = Set.of("Agile", "Scrum");

        if (languages.contains(skillName)) {
            return SkillCategory.LANGUAGE;
        }
        if (tools.contains(skillName)) {
            return SkillCategory.TOOL;
        }
        if (softSkills.contains(skillName)) {
            return SkillCategory.SOFT;
        }
        return SkillCategory.TECHNICAL;
    }

    private LocalDate parseYear(String yearStr) {
        if (yearStr == null || yearStr.isBlank()) {
            return null;
        }
        try {
            // Try extracting a 4-digit year
            Matcher yearMatcher = Pattern.compile("(\\d{4})").matcher(yearStr);
            if (yearMatcher.find()) {
                int year = Integer.parseInt(yearMatcher.group(1));
                return LocalDate.of(year, 1, 1);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private String buildExperienceSummary(List<ResumeExperience> experiences) {
        StringBuilder sb = new StringBuilder();
        for (ResumeExperience exp : experiences) {
            sb.append(exp.getTitle()).append(" at ").append(exp.getCompany());
            if (exp.getIsCurrent()) {
                sb.append(" (Current)");
            }
            sb.append("; ");
        }
        return TextSanitizer.truncate(sb.toString(), 1000);
    }

    private String buildEducationSummary(List<ResumeEducation> educations) {
        StringBuilder sb = new StringBuilder();
        for (ResumeEducation edu : educations) {
            if (edu.getDegree() != null) {
                sb.append(edu.getDegree());
            }
            if (edu.getFieldOfStudy() != null) {
                sb.append(" in ").append(edu.getFieldOfStudy());
            }
            sb.append(" from ").append(edu.getInstitution());
            if (edu.getGraduationYear() != null) {
                sb.append(" (").append(edu.getGraduationYear()).append(")");
            }
            sb.append("; ");
        }
        return TextSanitizer.truncate(sb.toString(), 1000);
    }
}
