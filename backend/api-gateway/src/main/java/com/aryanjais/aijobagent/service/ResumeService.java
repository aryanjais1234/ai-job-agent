package com.aryanjais.aijobagent.service;

import com.aryanjais.aijobagent.dto.response.ResumeResponse;
import com.aryanjais.aijobagent.entity.Resume;
import com.aryanjais.aijobagent.entity.ResumeEducation;
import com.aryanjais.aijobagent.entity.ResumeExperience;
import com.aryanjais.aijobagent.entity.ResumeSkill;
import com.aryanjais.aijobagent.entity.User;
import com.aryanjais.aijobagent.exception.FileStorageException;
import com.aryanjais.aijobagent.exception.ResourceNotFoundException;
import com.aryanjais.aijobagent.repository.ResumeEducationRepository;
import com.aryanjais.aijobagent.repository.ResumeExperienceRepository;
import com.aryanjais.aijobagent.repository.ResumeRepository;
import com.aryanjais.aijobagent.repository.ResumeSkillRepository;
import com.aryanjais.aijobagent.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private static final Logger log = LoggerFactory.getLogger(ResumeService.class);

    private final ResumeRepository resumeRepository;
    private final ResumeSkillRepository resumeSkillRepository;
    private final ResumeExperienceRepository resumeExperienceRepository;
    private final ResumeEducationRepository resumeEducationRepository;
    private final UserRepository userRepository;
    private final ResumeParserService resumeParserService;

    @Value("${app.storage.resume-upload-dir}")
    private String uploadDir;

    @Transactional
    public ResumeResponse uploadResume(Long userId, MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("application/pdf")
                && !contentType.equals("application/msword")
                && !contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))) {
            throw new FileStorageException("Only PDF and Word documents are accepted");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String storedFilename = UUID.randomUUID() + extension;

        try {
            Path uploadPath = Paths.get(uploadDir);
            Files.createDirectories(uploadPath);
            Path filePath = uploadPath.resolve(storedFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new FileStorageException("Failed to store file: " + originalFilename, e);
        }

        Resume resume = Resume.builder()
                .user(user)
                .originalFilename(originalFilename != null ? originalFilename : "unknown")
                .filePath(Paths.get(uploadDir, storedFilename).toString())
                .isPrimary(false)
                .fileSizeBytes((int) file.getSize())
                .build();

        resume = resumeRepository.save(resume);

        // Trigger resume parsing (T-3.4)
        try {
            resumeParserService.parseResume(resume.getId());
        } catch (Exception e) {
            // Log but don't fail the upload — parsing can be retried
            log.warn("Resume parsing failed for resume {}: {}", resume.getId(), e.getMessage());
        }

        return buildResumeResponse(resume);
    }

    public List<ResumeResponse> getResumes(Long userId) {
        List<Resume> resumes = resumeRepository.findByUserId(userId);
        return resumes.stream()
                .map(this::buildResumeResponse)
                .collect(Collectors.toList());
    }

    public ResumeResponse getResume(Long userId, Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with id: " + resumeId));

        if (!resume.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Resume not found with id: " + resumeId);
        }

        return buildResumeResponse(resume);
    }

    @Transactional
    public void setPrimary(Long userId, Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with id: " + resumeId));

        if (!resume.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Resume not found with id: " + resumeId);
        }

        List<Resume> userResumes = resumeRepository.findByUserId(userId);
        for (Resume r : userResumes) {
            r.setIsPrimary(false);
        }
        resumeRepository.saveAll(userResumes);

        resume.setIsPrimary(true);
        resumeRepository.save(resume);
    }

    @Transactional
    public void deleteResume(Long userId, Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with id: " + resumeId));

        if (!resume.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Resume not found with id: " + resumeId);
        }

        try {
            Path filePath = Paths.get(resume.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new FileStorageException("Failed to delete file: " + resume.getOriginalFilename(), e);
        }

        resumeRepository.delete(resume);
    }

    private ResumeResponse buildResumeResponse(Resume resume) {
        List<ResumeSkill> skills = resumeSkillRepository.findByResumeId(resume.getId());
        List<ResumeExperience> experiences = resumeExperienceRepository.findByResumeId(resume.getId());
        List<ResumeEducation> educations = resumeEducationRepository.findByResumeId(resume.getId());

        return ResumeResponse.builder()
                .id(resume.getId())
                .originalFilename(resume.getOriginalFilename())
                .filePath(resume.getFilePath())
                .isPrimary(resume.getIsPrimary())
                .fileSizeBytes(resume.getFileSizeBytes())
                .createdAt(resume.getCreatedAt())
                .skills(skills.stream().map(s -> ResumeResponse.SkillResponse.builder()
                        .id(s.getId())
                        .skillName(s.getSkillName())
                        .skillCategory(s.getSkillCategory() != null ? s.getSkillCategory().name() : null)
                        .proficiencyLevel(s.getProficiencyLevel() != null ? s.getProficiencyLevel().name() : null)
                        .yearsExperience(s.getYearsExperience())
                        .build()).collect(Collectors.toList()))
                .experiences(experiences.stream().map(e -> ResumeResponse.ExperienceResponse.builder()
                        .id(e.getId())
                        .company(e.getCompany())
                        .title(e.getTitle())
                        .startDate(e.getStartDate())
                        .endDate(e.getEndDate())
                        .description(e.getDescription())
                        .isCurrent(e.getIsCurrent())
                        .build()).collect(Collectors.toList()))
                .educations(educations.stream().map(ed -> ResumeResponse.EducationResponse.builder()
                        .id(ed.getId())
                        .institution(ed.getInstitution())
                        .degree(ed.getDegree())
                        .fieldOfStudy(ed.getFieldOfStudy())
                        .graduationYear(ed.getGraduationYear())
                        .gpa(ed.getGpa())
                        .build()).collect(Collectors.toList()))
                .build();
    }
}
