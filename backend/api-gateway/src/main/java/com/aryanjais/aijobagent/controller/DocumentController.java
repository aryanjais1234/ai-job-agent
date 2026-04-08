package com.aryanjais.aijobagent.controller;

import com.aryanjais.aijobagent.entity.CoverLetter;
import com.aryanjais.aijobagent.entity.TailoredResume;
import com.aryanjais.aijobagent.entity.User;
import com.aryanjais.aijobagent.exception.FileStorageException;
import com.aryanjais.aijobagent.exception.ResourceNotFoundException;
import com.aryanjais.aijobagent.repository.CoverLetterRepository;
import com.aryanjais.aijobagent.repository.TailoredResumeRepository;
import com.aryanjais.aijobagent.repository.UserRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Serves pre-generated PDF documents produced by the document-service.
 * PDFs are generated asynchronously by the document-service and stored on shared volume.
 */
@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
@Tag(name = "Documents")
public class DocumentController {

    private final TailoredResumeRepository tailoredResumeRepository;
    private final CoverLetterRepository coverLetterRepository;
    private final UserRepository userRepository;

    @GetMapping("/resume/{tailoredId}")
    public ResponseEntity<byte[]> downloadResumePdf(
            Authentication authentication, @PathVariable Long tailoredId) {
        User user = getUser(authentication);
        TailoredResume tailored = tailoredResumeRepository.findById(tailoredId)
                .orElseThrow(() -> new ResourceNotFoundException("Tailored resume not found: " + tailoredId));
        if (!tailored.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Tailored resume not found: " + tailoredId);
        }
        if (tailored.getPdfFilePath() == null) {
            throw new ResourceNotFoundException("PDF not yet generated. Please try again shortly.");
        }

        byte[] pdfBytes = readPdfBytes(tailored.getPdfFilePath());

        tailored.setIsDownloaded(true);
        tailoredResumeRepository.save(tailored);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=tailored_resume.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/cover-letter/{coverId}")
    public ResponseEntity<byte[]> downloadCoverLetterPdf(
            Authentication authentication, @PathVariable Long coverId) {
        User user = getUser(authentication);
        CoverLetter coverLetter = coverLetterRepository.findById(coverId)
                .orElseThrow(() -> new ResourceNotFoundException("Cover letter not found: " + coverId));
        if (!coverLetter.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Cover letter not found: " + coverId);
        }
        if (coverLetter.getPdfFilePath() == null) {
            throw new ResourceNotFoundException("PDF not yet generated. Please try again shortly.");
        }

        byte[] pdfBytes = readPdfBytes(coverLetter.getPdfFilePath());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=cover_letter.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    private User getUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private byte[] readPdfBytes(String filePath) {
        try {
            return Files.readAllBytes(Paths.get(filePath));
        } catch (IOException e) {
            throw new FileStorageException("Failed to read PDF file: " + filePath, e);
        }
    }
}
