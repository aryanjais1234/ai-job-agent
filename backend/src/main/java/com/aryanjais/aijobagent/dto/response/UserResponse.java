package com.aryanjais.aijobagent.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;

    private String email;

    private String fullName;

    private String phone;

    private String linkedinUrl;

    private String githubUrl;

    private String portfolioUrl;

    private String location;

    private Integer experienceYears;

    private Boolean isActive;

    private Boolean emailVerified;

    private LocalDateTime createdAt;
}
