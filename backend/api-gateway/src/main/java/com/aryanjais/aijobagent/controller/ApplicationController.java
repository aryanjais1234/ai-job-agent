package com.aryanjais.aijobagent.controller;

import com.aryanjais.aijobagent.dto.request.CreateApplicationRequest;
import com.aryanjais.aijobagent.dto.request.UpdateApplicationRequest;
import com.aryanjais.aijobagent.dto.response.ApplicationResponse;
import com.aryanjais.aijobagent.entity.User;
import com.aryanjais.aijobagent.entity.enums.ApplicationStatus;
import com.aryanjais.aijobagent.exception.ResourceNotFoundException;
import com.aryanjais.aijobagent.repository.UserRepository;
import com.aryanjais.aijobagent.service.ApplicationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
@Tag(name = "Applications")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Page<ApplicationResponse>> getApplications(
            Authentication authentication,
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        User user = getUser(authentication);
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(applicationService.getApplications(user.getId(), status, pageable));
    }

    @PostMapping
    public ResponseEntity<ApplicationResponse> createApplication(
            Authentication authentication,
            @Valid @RequestBody CreateApplicationRequest request) {
        User user = getUser(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(applicationService.createApplication(user.getId(), request.getJobId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApplicationResponse> updateApplication(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody UpdateApplicationRequest request) {
        User user = getUser(authentication);
        return ResponseEntity.ok(applicationService.updateApplication(user.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(
            Authentication authentication, @PathVariable Long id) {
        User user = getUser(authentication);
        applicationService.deleteApplication(user.getId(), id);
        return ResponseEntity.noContent().build();
    }

    private User getUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
