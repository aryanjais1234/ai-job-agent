package com.aryanjais.aijobagent.repository;

import com.aryanjais.aijobagent.entity.JobMatch;
import com.aryanjais.aijobagent.entity.enums.MatchStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface JobMatchRepository extends JpaRepository<JobMatch, Long> {

    Page<JobMatch> findByUserIdOrderByOverallScoreDesc(Long userId, Pageable pageable);

    Page<JobMatch> findByUserIdAndStatus(Long userId, MatchStatus status, Pageable pageable);

    Page<JobMatch> findByUserIdAndOverallScoreGreaterThanEqualOrderByOverallScoreDesc(
            Long userId, BigDecimal minScore, Pageable pageable);

    void deleteByUserId(Long userId);
}
