package com.aryanjais.aijobagent.repository;

import com.aryanjais.aijobagent.entity.CoverLetter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CoverLetterRepository extends JpaRepository<CoverLetter, Long> {

    Optional<CoverLetter> findByUserIdAndJobId(Long userId, Long jobId);

    void deleteByUserId(Long userId);
}
