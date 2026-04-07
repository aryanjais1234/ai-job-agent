package com.aryanjais.aijobagent.controller;

import com.aryanjais.aijobagent.dto.response.MessageResponse;
import com.aryanjais.aijobagent.dto.response.ResumeResponse;
import com.aryanjais.aijobagent.entity.User;
import com.aryanjais.aijobagent.exception.ResourceNotFoundException;
import com.aryanjais.aijobagent.repository.UserRepository;
import com.aryanjais.aijobagent.service.ResumeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/resumes")
@RequiredArgsConstructor
@Tag(name = "Resumes")
public class ResumeController {

    private final ResumeService resumeService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<ResumeResponse> uploadResume(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) {
        User user = getCurrentUser(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(resumeService.uploadResume(user.getId(), file));
    }

    @GetMapping
    public ResponseEntity<List<ResumeResponse>> getResumes(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        return ResponseEntity.ok(resumeService.getResumes(user.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResumeResponse> getResume(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        User user = getCurrentUser(userDetails);
        return ResponseEntity.ok(resumeService.getResume(user.getId(), id));
    }

    @PutMapping("/{id}/primary")
    public ResponseEntity<MessageResponse> setPrimary(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        User user = getCurrentUser(userDetails);
        resumeService.setPrimary(user.getId(), id);
        return ResponseEntity.ok(MessageResponse.builder().message("Resume set as primary").build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResume(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        User user = getCurrentUser(userDetails);
        resumeService.deleteResume(user.getId(), id);
        return ResponseEntity.noContent().build();
    }

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
