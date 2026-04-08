package com.aryanjais.aijobagent.repository;

import com.aryanjais.aijobagent.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {

    List<Resume> findByUserId(Long userId);

    Optional<Resume> findByUserIdAndIsPrimary(Long userId, boolean isPrimary);

    void deleteByUserId(Long userId);
}
