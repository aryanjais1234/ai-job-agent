package com.aryanjais.aijobagent.repository;

import com.aryanjais.aijobagent.entity.JobAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobAnalysisRepository extends JpaRepository<JobAnalysis, Long> {

    Optional<JobAnalysis> findByJobId(Long jobId);
}
