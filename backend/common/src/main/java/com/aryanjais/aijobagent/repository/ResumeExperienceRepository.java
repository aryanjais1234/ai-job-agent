package com.aryanjais.aijobagent.repository;

import com.aryanjais.aijobagent.entity.ResumeExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResumeExperienceRepository extends JpaRepository<ResumeExperience, Long> {

    List<ResumeExperience> findByResumeId(Long resumeId);
}
