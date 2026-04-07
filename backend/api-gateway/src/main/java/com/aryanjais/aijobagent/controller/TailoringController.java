package com.aryanjais.aijobagent.controller;

import com.aryanjais.aijobagent.dto.response.CoverLetterResponse;
import com.aryanjais.aijobagent.dto.response.MessageResponse;
import com.aryanjais.aijobagent.dto.response.TailoredResumeResponse;
import com.aryanjais.aijobagent.entity.CoverLetter;
import com.aryanjais.aijobagent.entity.TailoredResume;
import com.aryanjais.aijobagent.entity.User;
import com.aryanjais.aijobagent.exception.ResourceNotFoundException;
import com.aryanjais.aijobagent.messaging.producer.JobTailorProducer;
import com.aryanjais.aijobagent.repository.CoverLetterRepository;
import com.aryanjais.aijobagent.repository.TailoredResumeRepository;
import com.aryanjais.aijobagent.repository.UserRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tailor")
@RequiredArgsConstructor
@Tag(name = "Tailoring")
public class TailoringController {

    private final JobTailorProducer jobTailorProducer;
    private final TailoredResumeRepository tailoredResumeRepository;
    private final CoverLetterRepository coverLetterRepository;
    private final UserRepository userRepository;

    /**
     * Triggers async resume tailoring via the document-service message queue.
     * Returns ACCEPTED — the tailored result can be polled via GET /{id}.
     */
    @PostMapping("/{jobId}")
    public ResponseEntity<MessageResponse> tailorResume(
            Authentication authentication, @PathVariable Long jobId) {
        User user = getUser(authentication);
        // Check if already tailored
        var existing = tailoredResumeRepository.findByUserIdAndJobId(user.getId(), jobId);
        if (existing.isPresent()) {
            return ResponseEntity.ok(MessageResponse.builder().message("Resume already tailored for this job").build());
        }
        // Publish async tailoring request to document-service
        jobTailorProducer.publishForTailoring(user.getId(), jobId);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(MessageResponse.builder().message("Resume tailoring queued — check back shortly").build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TailoredResumeResponse> getTailoredResume(
            Authentication authentication, @PathVariable Long id) {
        User user = getUser(authentication);
        TailoredResume tailored = tailoredResumeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tailored resume not found: " + id));
        if (!tailored.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Tailored resume not found: " + id);
        }
        return ResponseEntity.ok(toResponse(tailored));
    }

    /**
     * Triggers async cover letter generation via the document-service.
     */
    @PostMapping("/cover-letter/{jobId}")
    public ResponseEntity<MessageResponse> generateCoverLetter(
            Authentication authentication, @PathVariable Long jobId) {
        User user = getUser(authentication);
        var existing = coverLetterRepository.findByUserIdAndJobId(user.getId(), jobId);
        if (existing.isPresent()) {
            return ResponseEntity.ok(MessageResponse.builder().message("Cover letter already generated for this job").build());
        }
        // The document-service's JobTailorConsumer generates both tailored resume and cover letter
        jobTailorProducer.publishForTailoring(user.getId(), jobId);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(MessageResponse.builder().message("Cover letter generation queued — check back shortly").build());
    }

    @GetMapping("/cover-letter/{id}/view")
    public ResponseEntity<CoverLetterResponse> getCoverLetter(
            Authentication authentication, @PathVariable Long id) {
        User user = getUser(authentication);
        CoverLetter letter = coverLetterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cover letter not found: " + id));
        if (!letter.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Cover letter not found: " + id);
        }
        return ResponseEntity.ok(toCoverLetterResponse(letter));
    }

    private User getUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private TailoredResumeResponse toResponse(TailoredResume tr) {
        return TailoredResumeResponse.builder()
                .id(tr.getId())
                .jobId(tr.getJob().getId())
                .jobTitle(tr.getJob().getTitle())
                .company(tr.getJob().getCompany())
                .tailoredContent(tr.getTailoredContent())
                .modificationsLog(tr.getModificationsLog())
                .atsScore(tr.getAtsScore())
                .pdfAvailable(tr.getPdfFilePath() != null)
                .createdAt(tr.getCreatedAt())
                .build();
    }

    private CoverLetterResponse toCoverLetterResponse(CoverLetter cl) {
        return CoverLetterResponse.builder()
                .id(cl.getId())
                .jobId(cl.getJob().getId())
                .jobTitle(cl.getJob().getTitle())
                .company(cl.getJob().getCompany())
                .content(cl.getContent())
                .pdfAvailable(cl.getPdfFilePath() != null)
                .createdAt(cl.getCreatedAt())
                .build();
    }
}
