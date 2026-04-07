package com.aryanjais.aijobagent.dto.request;

import com.aryanjais.aijobagent.entity.enums.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateApplicationRequest {

    private ApplicationStatus status;

    private String notes;

    private LocalDateTime interviewDate;
}
