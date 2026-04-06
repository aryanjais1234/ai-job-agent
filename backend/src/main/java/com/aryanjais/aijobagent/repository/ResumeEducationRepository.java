package com.aryanjais.aijobagent.repository;

import com.aryanjais.aijobagent.entity.ResumeEducation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResumeEducationRepository extends JpaRepository<ResumeEducation, Long> {

    List<ResumeEducation> findByResumeId(Long resumeId);
}
