package com.aryanjais.aijobagent.controller;

import com.aryanjais.aijobagent.dto.request.UpdatePreferencesRequest;
import com.aryanjais.aijobagent.dto.request.UpdateProfileRequest;
import com.aryanjais.aijobagent.dto.response.UserResponse;
import com.aryanjais.aijobagent.entity.User;
import com.aryanjais.aijobagent.entity.UserPreference;
import com.aryanjais.aijobagent.exception.ResourceNotFoundException;
import com.aryanjais.aijobagent.repository.UserRepository;
import com.aryanjais.aijobagent.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        return ResponseEntity.ok(userService.getProfile(user.getId()));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        User user = getCurrentUser(userDetails);
        return ResponseEntity.ok(userService.updateProfile(user.getId(), request));
    }

    @GetMapping("/preferences")
    public ResponseEntity<UserPreference> getPreferences(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        return ResponseEntity.ok(userService.getPreferences(user.getId()));
    }

    @PutMapping("/preferences")
    public ResponseEntity<UserPreference> updatePreferences(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdatePreferencesRequest request) {
        User user = getCurrentUser(userDetails);
        return ResponseEntity.ok(userService.updatePreferences(user.getId(), request));
    }

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
