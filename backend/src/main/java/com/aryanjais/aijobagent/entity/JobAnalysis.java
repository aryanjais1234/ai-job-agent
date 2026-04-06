package com.aryanjais.aijobagent.entity;

import com.aryanjais.aijobagent.entity.enums.RemoteType;
import com.aryanjais.aijobagent.entity.enums.SeniorityLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "job_analyses")
public class JobAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false, unique = true)
    private Job job;

    @Column(name = "required_skills", columnDefinition = "json")
    private String requiredSkills;

    @Column(name = "nice_to_have_skills", columnDefinition = "json")
    private String niceToHaveSkills;

    @Column(name = "experience_min")
    private Integer experienceMin;

    @Column(name = "experience_max")
    private Integer experienceMax;

    @Column(name = "education_required", length = 100)
    private String educationRequired;

    @Column(name = "domain", length = 100)
    private String domain;

    @Column(name = "keywords", columnDefinition = "json")
    private String keywords;

    @Enumerated(EnumType.STRING)
    @Column(name = "seniority_level")
    private SeniorityLevel seniorityLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "remote_type")
    private RemoteType remoteType;

    @Column(name = "analyzed_at", nullable = false)
    private LocalDateTime analyzedAt;
}
