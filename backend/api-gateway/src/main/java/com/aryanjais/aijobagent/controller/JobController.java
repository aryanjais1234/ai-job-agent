package com.aryanjais.aijobagent.controller;

import com.aryanjais.aijobagent.dto.response.JobMatchResponse;
import com.aryanjais.aijobagent.dto.response.JobResponse;
import com.aryanjais.aijobagent.entity.User;
import com.aryanjais.aijobagent.repository.UserRepository;
import com.aryanjais.aijobagent.service.JobService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Job listing and match score endpoints (Phase 3 API).
 */
@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
@Tag(name = "Jobs")
public class JobController {

    private final JobService jobService;
    private final UserRepository userRepository;

    /**
     * GET /api/v1/jobs — List jobs with optional search by keyword and location.
     */
    @GetMapping
    public ResponseEntity<Page<JobResponse>> listJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);

        boolean hasSearch = (keyword != null && !keyword.isBlank())
                || (location != null && !location.isBlank());

        if (hasSearch) {
            return ResponseEntity.ok(jobService.searchJobs(keyword, location, pageable));
        }
        return ResponseEntity.ok(jobService.listJobs(pageable));
    }

    /**
     * GET /api/v1/jobs/{id} — Get a single job with analysis details.
     */
    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getJob(@PathVariable Long id) {
        return ResponseEntity.ok(jobService.getJob(id));
    }

    /**
     * GET /api/v1/jobs/matches — Get user's job matches sorted by score with optional minScore filter.
     */
    @GetMapping("/matches")
    public ResponseEntity<Page<JobMatchResponse>> getMatches(
            Authentication authentication,
            @RequestParam(required = false) Double minScore,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new com.aryanjais.aijobagent.exception.ResourceNotFoundException(
                        "User not found"));

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(jobService.getMatchesForUser(user.getId(), minScore, pageable));
    }
}
