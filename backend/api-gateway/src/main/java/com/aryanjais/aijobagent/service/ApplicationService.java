package com.aryanjais.aijobagent.service;

import com.aryanjais.aijobagent.dto.request.UpdateApplicationRequest;
import com.aryanjais.aijobagent.dto.response.ApplicationResponse;
import com.aryanjais.aijobagent.entity.*;
import com.aryanjais.aijobagent.entity.enums.ApplicationStatus;
import com.aryanjais.aijobagent.exception.DuplicateResourceException;
import com.aryanjais.aijobagent.exception.ResourceNotFoundException;
import com.aryanjais.aijobagent.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationService.class);

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final TailoredResumeRepository tailoredResumeRepository;
    private final CoverLetterRepository coverLetterRepository;

    public Page<ApplicationResponse> getApplications(Long userId, ApplicationStatus status, Pageable pageable) {
        Page<Application> apps;
        if (status != null) {
            apps = applicationRepository.findByUserIdAndStatus(userId, status, pageable);
        } else {
            apps = applicationRepository.findByUserId(userId, pageable);
        }
        return apps.map(this::toResponse);
    }

    @Transactional
    public ApplicationResponse createApplication(Long userId, Long jobId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + jobId));

        // Check for duplicate
        Page<Application> existing = applicationRepository.findByUserId(userId, Pageable.unpaged());
        boolean alreadyApplied = existing.getContent().stream()
                .anyMatch(a -> a.getJob().getId().equals(jobId));
        if (alreadyApplied) {
            throw new DuplicateResourceException("Application already exists for this job");
        }

        TailoredResume tailoredResume = tailoredResumeRepository.findByUserIdAndJobId(userId, jobId).orElse(null);
        CoverLetter coverLetter = coverLetterRepository.findByUserIdAndJobId(userId, jobId).orElse(null);

        Application application = Application.builder()
                .user(user)
                .job(job)
                .tailoredResume(tailoredResume)
                .coverLetter(coverLetter)
                .status(ApplicationStatus.PENDING)
                .appliedAt(LocalDateTime.now())
                .build();

        Application saved = applicationRepository.save(application);
        log.info("Application created: user={}, job={}", userId, jobId);
        return toResponse(saved);
    }

    @Transactional
    public ApplicationResponse updateApplication(Long userId, Long applicationId, UpdateApplicationRequest request) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));

        if (!application.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Application not found: " + applicationId);
        }

        if (request.getStatus() != null) {
            application.setStatus(request.getStatus());
        }
        if (request.getNotes() != null) {
            application.setNotes(request.getNotes());
        }
        if (request.getInterviewDate() != null) {
            application.setInterviewDate(request.getInterviewDate());
        }

        Application saved = applicationRepository.save(application);
        log.info("Application updated: id={}, status={}", applicationId, saved.getStatus());
        return toResponse(saved);
    }

    @Transactional
    public void deleteApplication(Long userId, Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));

        if (!application.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Application not found: " + applicationId);
        }

        applicationRepository.delete(application);
        log.info("Application deleted: id={}", applicationId);
    }

    private ApplicationResponse toResponse(Application app) {
        return ApplicationResponse.builder()
                .id(app.getId())
                .jobId(app.getJob().getId())
                .jobTitle(app.getJob().getTitle())
                .company(app.getJob().getCompany())
                .location(app.getJob().getLocation())
                .status(app.getStatus().name())
                .appliedAt(app.getAppliedAt())
                .interviewDate(app.getInterviewDate())
                .notes(app.getNotes())
                .hasTailoredResume(app.getTailoredResume() != null)
                .hasCoverLetter(app.getCoverLetter() != null)
                .createdAt(app.getCreatedAt())
                .build();
    }
}
