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
public class ScrapeLogResponse {

    private Long id;

    private String platform;

    private String status;

    private Integer jobsFound;

    private Integer jobsNew;

    private Integer jobsUpdated;

    private String errorMessage;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;
}
