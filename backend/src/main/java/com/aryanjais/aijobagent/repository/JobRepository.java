package com.aryanjais.aijobagent.repository;

import com.aryanjais.aijobagent.entity.Job;
import com.aryanjais.aijobagent.entity.enums.SourcePlatform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    Optional<Job> findBySourcePlatformAndSourceUrl(SourcePlatform platform, String sourceUrl);
}
