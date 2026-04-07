package com.aryanjais.aijobagent.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequest {

    private String fullName;

    private String phone;

    private String linkedinUrl;

    private String githubUrl;

    private String portfolioUrl;

    private String location;

    private Integer experienceYears;
}
