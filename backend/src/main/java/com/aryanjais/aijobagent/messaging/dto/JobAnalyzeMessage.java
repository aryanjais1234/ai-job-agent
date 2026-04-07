package com.aryanjais.aijobagent.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Message DTO for the jobs.analyze queue.
 * Signals that a job needs AI analysis.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobAnalyzeMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long jobId;
}
