package com.aryanjais.aijobagent.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePreferencesRequest {

    private List<String> jobTitles;

    private List<String> locations;

    private BigDecimal minSalary;

    private BigDecimal maxSalary;

    private List<String> jobTypes;

    private Boolean remoteOk;

    private List<String> experienceLevels;

    private List<String> skillsRequired;

    private List<String> industries;
}
