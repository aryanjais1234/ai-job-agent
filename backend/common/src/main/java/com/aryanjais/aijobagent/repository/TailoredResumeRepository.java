package com.aryanjais.aijobagent.repository;

import com.aryanjais.aijobagent.entity.TailoredResume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TailoredResumeRepository extends JpaRepository<TailoredResume, Long> {

    Optional<TailoredResume> findByUserIdAndJobId(Long userId, Long jobId);

    void deleteByUserId(Long userId);
}
